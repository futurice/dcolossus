package com.futurice.dcolossus

import colossus.core.ServerContext
import colossus.service.{Callback, Protocol}
import colossus.service.Protocol.PartialHandler
import fi.veikkaus.dcontext.MutableDContext

/**
  * Created by arau on 5.7.2016.
  */
class CompositeDService[C <: Protocol](services:Seq[DService[C]]) extends DService[C] {
  override def handle  =
    new PartialHandler[C] {
      def serviceFor(x: C#Input)  =
        services.find( s => s.handle.isDefinedAt(x) )
      override def isDefinedAt(x: C#Input) : Boolean =
        serviceFor(x).isDefined
      override def apply(x: C#Input): Callback[C#Output] =
        serviceFor(x).get.handle.apply(x)
    }
}
