package com.futurice.dcolossus

import colossus.core.ServerContext
import colossus.service.GenRequestHandler.{ErrorHandler, PartialHandler}
import colossus.service.Protocol

/**
  * Created by arau on 4.7.2016.
  */

trait DService[C <: Protocol] {
  def handle : PartialHandler[C]
  def unhandledError: ErrorHandler[C]
}

trait DServiceProvider[C <: Protocol] {
  def apply(serverContext:ServerContext) : DService[C]
}

class ProxyServiceProvider[C <: Protocol](provider:DServiceProvider[C]) extends DServiceProvider[C] {
  override def apply(serverContext: ServerContext): DService[C] = {
    new DService[C]  {
      override def handle: PartialHandler[C] = {
        provider(serverContext).handle
      }

      override def unhandledError = {
        provider(serverContext).unhandledError
      }
    }
  }
}
