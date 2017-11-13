package com.futurice.dcolossus

import colossus.core.ServerContext
import colossus.service.GenRequestHandler.{ErrorHandler, PartialHandler}
import colossus.service.{Callback, ProcessingFailure, Protocol}

/**
  * Created by arau on 5.7.2016.
  */
class CompositeDService[C <: Protocol](services:Seq[DService[C]]) extends DService[C] {
  override def handle  =
    new PartialHandler[C] {
      def serviceFor(x: C#Request)  =
        services.find( s => s.handle.isDefinedAt(x) )
      override def isDefinedAt(x: C#Request) : Boolean =
        serviceFor(x).isDefined
      override def apply(x: C#Request): Callback[C#Response] =
        serviceFor(x).get.handle.apply(x)
    }

  override def unhandledError = new ErrorHandler[C] {
    def serviceFor(x: ProcessingFailure[C#Request]) = services.find { s =>
      s.unhandledError.isDefinedAt(x)
    }
    override def isDefinedAt(x: ProcessingFailure[C#Request]) = {
      serviceFor(x).isDefined
    }
    override def apply(x: ProcessingFailure[C#Request]) : C#Response = {
      serviceFor(x).get.unhandledError.apply(x)
    }
  }
}
