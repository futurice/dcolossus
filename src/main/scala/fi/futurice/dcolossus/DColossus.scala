package fi.futurice.dcolossus

import akka.actor.ActorSystem
import colossus.IOSystem
import colossus.core.{Initializer, Server, ServerRef}
import colossus.protocols.http.Http
import colossus.service.ServiceConfig
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
    ActorSystem("dcolossus")
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

  def httpDServerCval(name:String,
                      port:Int,
                      className:String) : ContextVal[ServerRef] = {
    cvalc(serverPrefix + name) { c =>
      val sys = c.system.get
      implicit val actors = actorSystem(c)
      implicit val system = ioSystem(c)
      Server.start("hello", 9000) { worker => new Initializer(worker) {
          def onConnect = context =>
            new DHttpServiceProxy(
              ServiceConfig.Default,
              context,
              c,
              sys.classLoader().newProxyInstance(classOf[DService[Http]], className))
        }
      }(system)
    } { ref =>
      ref.shutdown
    }
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