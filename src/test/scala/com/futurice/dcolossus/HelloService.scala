package com.futurice.dcolossus

import javax.swing.tree.DefaultMutableTreeNode

import colossus.IOSystem
import colossus.core.ServerContext
import colossus.service.Callback
import fi.veikkaus.dcontext._
import colossus._
import core._
import service._
import protocols.http._
import UrlParsing._
import HttpMethod._
import akka.actor.ActorSystem

class HelloService extends Contextual(ExampleContext.prefix + "services.hello") with DService[Http] {
  val message = cval("message") { c =>
    "HELLO"
  }
  def handle(sc:ServerContext, c:MutableDContext) = {
    case request @ Get on Root / "hello" => {
      Callback.successful(request.ok(message(c)))
    }
    case request @ Get on Root => {
      Callback.successful(request.ok(c.keySet.mkString(",")))
    }
  }
}

