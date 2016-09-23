package com.futurice.dcolossus

import colossus.core.ServerContext
import colossus.protocols.http.HttpRequest
import colossus.service.Protocol
import colossus.service.Protocol.PartialHandler
import fi.veikkaus.dcontext.MutableDContext

/**
  * Created by arau on 4.7.2016.
  */
trait DService[C <: Protocol] {

  def handle(sc:ServerContext,
             c:MutableDContext) : PartialHandler[C]

}
