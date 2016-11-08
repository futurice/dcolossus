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

class HelloService(c:MutableDContext, sc:ServerContext) extends DService[Http] {
  def handle = {
    case request @ Get on Root / "hello" => {
      Callback.successful(request.ok("hello!"))
    }
    case request @ Get on Root => {
      Callback.successful(request.ok(c.keySet.mkString(",")))
    }
  }
}

class HelloServiceProvider(c:MutableDContext) extends DServiceProvider[Http] {
  def apply(serverContext:ServerContext) = {
    new HelloService(c, serverContext)
  }
}