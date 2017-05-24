package com.futurice.dcolossus

import akka.actor.ActorSystem
import colossus.IOSystem
import colossus.core._
import colossus.protocols.http.Http
import colossus.service.{Protocol, ServiceCodecProvider, ServiceConfig}
import com.typesafe.config.ConfigFactory
import fi.veikkaus.dcontext.{ContextTask, ContextVal, Contextual, MutableDContext}

/**
  * Created by arau on 4.7.2016.
  */
class DColossus(name:String) extends Contextual(name) {

  def serverPrefix = "servers."

  def instances(context: MutableDContext) = {
    context.keySet.filter(_.startsWith(name + "." + serverPrefix))
  }

  val closeCmd = cval("close") { c =>
    new ContextTask {
      override def run(c: MutableDContext, args: Array[String]): Unit = {
        close(c)
      }
    }
  }

  val actorSystem = cvalc("actorSystem") { c =>
    closeCmd(c)
    ActorSystem("dcolossus") // , ConfigFactory.defaultReference(c.system.map(_.classLoader()).getOrElse(getClass.getClassLoader)))
  } { sys => sys.shutdown() }

  val ioSystem = cvalc("ioSystem") { c =>
    closeCmd(c)
    IOSystem()(actorSystem(c))
  } { sys => sys.shutdown() }

  def close(c:MutableDContext): Unit = {
    instances(c).foreach {
      c.remove(_)
    }
    actorSystem.reset(c)
    ioSystem.reset(c)
    closeCmd.reset(c)
  }

  def dserverCval[C <: Protocol](name:String,
                                 port:Int,
                                 className:String,
                                 config:ServiceConfig,
                                 codecProvider: ServiceCodecProvider[C])
    : ContextVal[ServerRef] = {
    cvalc(serverPrefix + name) { c =>
      val sys = c.system.get
      implicit val actors = actorSystem(c)
      implicit val system = ioSystem(c)
      Server.start(name, port) { worker => new Initializer(worker) {
        def onConnect = serverContext =>
          new DServiceProxy[C](
            config,
            codecProvider,
            serverContext,
            sys.classLoader()
               .newProxyInstance(
                 classOf[DService[C]],
                 className,
                 Array(classOf[MutableDContext], classOf[ServerContext]),
                 Array(c, serverContext)))
      }
      }(system)
    } { ref =>
      ref.shutdown
    }
  }
  def dserverCval2[C <: Protocol](name:String,
                                 port:Int,
                                 className:String,
                                 config:ServiceConfig,
                                 codecProvider: ServiceCodecProvider[C])
  : ContextVal[(ProxyServiceProvider[C], ServerRef)] = {
    cvalc(serverPrefix + name) { c =>
      val sys = c.system.get
      implicit val actors = actorSystem(c)
      implicit val system = ioSystem(c)

      val serviceProvider =
        new ProxyServiceProvider[C](
          sys.classLoader()
          .newProxyInstance(
            classOf[DServiceProvider[C]],
            className,
            Array(classOf[MutableDContext]),
            Array(c)))

      (serviceProvider,
       Server.start(name, port) { worker => new Initializer(worker) {
        def onConnect = serverContext =>
          new DServiceProxy[C](
            config,
            codecProvider,
            serverContext,
            serviceProvider(serverContext))
        }
      }(system))
    } { case (serviceProvider, ref) =>
      ref.shutdown // shut down the HTTP service, before the server state
      serviceProvider.close
    }
  }

  def httpDServerCval(name:String,
                      port:Int,
                      className:String) : ContextVal[ServerRef] = {
    dserverCval[Http](name,
                      port,
                      className,
                      ServiceConfig.Default,
                      Http.defaults.httpServerDefaults)
  }
  def httpDServerCval(className:String, port:Int) : ContextVal[ServerRef] = {
    httpDServerCval(className.split('.').last, port, className)
  }
  def httpDServerCval[C <: DService[Http]](className:Class[C], port:Int) : ContextVal[ServerRef] = {
    httpDServerCval(className.getName, port)
  }
  def httpDServerCval[C <: DService[Http]](serverName:String, port:Int, className:Class[C]) : ContextVal[ServerRef] = {
    httpDServerCval(serverName, port, className.getName)
  }
}

object DColossus {
  def apply(name:String) = new DColossus(name)
  def apply() = new DColossus("dcolossus")
}