package com.futurice.dcolossus

import java.io.Closeable

import colossus.protocols.http.HttpService
import akka.util.ByteString
import colossus.IOSystem
import colossus.core._
import colossus.protocols.http.HttpMethod._
import colossus.protocols.http.UrlParsing._
import colossus.protocols.http._
import colossus.protocols.redis.Redis.defaults._
import colossus.protocols.redis._
import colossus.service.Callback.Implicits._
import colossus.service.Protocol._
import colossus.service._
import fi.veikkaus.dcontext.MutableDContext

class DServiceProxy[C <: Protocol](config:ServiceConfig,
                                   codecProvider: ServiceCodecProvider[C],
                                   serverContext:ServerContext,
                                   service:DService[C])
  extends Service[C](config, serverContext)(codecProvider) {
  override def handle =
    new PartialHandler[C] {
      override def isDefinedAt(x: C#Input): Boolean =
        service.handle.isDefinedAt(x)
      override def apply(v1: C#Input): Callback[C#Output] =
        service.handle.apply(v1)
    }
}

