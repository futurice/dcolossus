package com.futurice.dcolossus

import fi.veikkaus.dcontext.{ContextTask, HashMapDContext, MutableDContext}

/**
  * Created by arau on 4.7.2016.
  */
class ExampleContext extends HashMapDContext {

  val server = DColossus().httpDServerCval("example", 9000, classOf[ExampleService])

  val prefix = ExampleContext.prefix

  put(prefix + "start",
      new ContextTask {
        override def run(context: MutableDContext, args: Array[String]): Unit = {
          server(context)
        }
      })

  put(prefix + "stop",
    new ContextTask {
      override def run(context: MutableDContext, args: Array[String]): Unit = {
        server.reset(context)
      }
    })

  put(prefix + "clean",
    new ContextTask {
      override def run(context: MutableDContext, args: Array[String]): Unit = {
        context.keySet.filter(_.startsWith(prefix)).foreach {
          context.remove(_)
        }
      }
    })

}

object ExampleContext {

  def prefix = "exampleServer."

}