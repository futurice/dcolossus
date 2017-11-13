package com.futurice.dcolossus

import colossus.core._
import colossus.service.GenRequestHandler.{ErrorHandler, PartialHandler}
import colossus.service._

class DServiceProxy[C <: Protocol](config:ServiceConfig,
                                   serverContext:ServerContext,
                                   service:DService[C])
  extends GenRequestHandler[C](serverContext, config) {
  override def handle =
    new PartialHandler[C] {
      override def isDefinedAt(x: C#Request): Boolean =
        service.handle.isDefinedAt(x)
      override def apply(v1: C#Request): Callback[C#Response] =
        service.handle.apply(v1)
    }

  override def unhandledError = new ErrorHandler[C] {
    override def isDefinedAt(x: ProcessingFailure[C#Request]) = {
      service.unhandledError.isDefinedAt(x)
    }
    override def apply(v1: ProcessingFailure[C#Request]) : C#Response = {
      service.unhandledError.apply(v1)
    }
  }
}

