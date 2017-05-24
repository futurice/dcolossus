package com.futurice.dcolossus

import java.io.Closeable

import colossus.core.ServerContext
import colossus.protocols.http.HttpRequest
import colossus.service.{Protocol, Service}
import colossus.service.Protocol.PartialHandler
import fi.veikkaus.dcontext.{DynamicClassLoader, MutableDContext}

/**
  * Created by arau on 4.7.2016.
  */

trait DService[C <: Protocol] {
  def handle : PartialHandler[C]
}

trait DServiceProvider[C <: Protocol] extends Closeable {
  def apply(serverContext:ServerContext) : DService[C]
  def close = {}
}

class ProxyServiceProvider[C <: Protocol](provider:DServiceProvider[C]) extends DServiceProvider[C] {
  override def apply(serverContext: ServerContext): DService[C] = {
    new DService[C]  {
      override def handle: PartialHandler[C] = {
        provider(serverContext).handle
      }
    }
  }
  def close = {
    provider.close()
  }
}
