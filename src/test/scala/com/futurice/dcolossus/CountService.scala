package com.futurice.dcolossus

import colossus.core.ServerContext
import colossus.protocols.http.Http
import colossus.protocols.http.HttpMethod.Get
import colossus.protocols.http.UrlParsing.{/, Root, on}
import colossus.service.Callback
import fi.veikkaus.dcontext.{Contextual, MutableDContext}

/**
  * Created by arau on 5.7.2016.
  */
class CountService(c:MutableDContext, sc:ServerContext) extends Contextual(ExampleContext.prefix + "services.count") with DService[Http] {

  def countName = contextName + ".value"

  def handle = {
    case request @ Get on Root / "count" => {
      val i = c.get[Int](countName).getOrElse(0)
      val rv = "" + i
      c.put(countName, (i + 1))
      Callback.successful(request.ok(rv))
    }
  }
}
