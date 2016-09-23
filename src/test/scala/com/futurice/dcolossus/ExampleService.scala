package com.futurice.dcolossus

/**
  * Created by arau on 5.7.2016.
  */
class ExampleService
  extends CompositeDService(Seq(new HelloService, new CountService)) {
}
