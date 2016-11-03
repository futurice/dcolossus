package com.futurice.dcolossus

import colossus.core.ServerContext
import fi.veikkaus.dcontext.MutableDContext

/**
  * Created by arau on 5.7.2016.
  */
class ExampleService(c:MutableDContext, sc:ServerContext)
  extends CompositeDService(Seq(new HelloService(c, sc), new CountService(c, sc))) {
}
