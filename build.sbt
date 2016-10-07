//
// dcolossus
//
// dcontext wrapper on top of colossus
//
// makes it possible to have fast iteration cycle with a stateful/RESTless
// server with lots of data.
//

val scalaMajorVersion = "2.11"
val scalaMinorVersion = "7"
val sparkVersion = "1.6.0"

scalaVersion := f"${scalaMajorVersion}.${scalaMinorVersion}"

crossScalaVersions := Seq("2.10.6")

version := "0.1"

connectInput      in Test := true
parallelExecution in Test := false

libraryDependencies ++= Seq(
  "com.tumblr" %% "colossus" % "0.8.1",
  "fi.veikkaus" %% "dcontext" % "0.2-SNAPSHOT",
  "com.futurice" %% "testtoys" % "0.2" % "test"
)

lazy val testsh =
  taskKey[Unit]("interactive shell for running tasks in a JVM instance, while code may be modified")

lazy val dcolossus = (project in file(".")).
  settings(
    name := "dcolossus",
    organization := "com.futurice",
    testsh := {
      val mainClass = "fi.veikkaus.dcontext.Console"
      val keyPath =  f"target/scala-${scalaMajorVersion}/test-classes"
      val selector = (path:File) => {
        val p = path.getPath
        p.contains(keyPath)
      }
      val classpath =
        (fullClasspath in Test)
          .value
          .map(i=>i.data)
      val staticCP = classpath.filter(!selector(_))
      val dynamicCP = classpath.filter(selector)
      //    println("static cp:" + staticCP)
      //    println("dynamic cp:" + dynamicCP)
      val options =  (javaOptions in Test).value
      val log = (streams in Test).value.log
      val args = Seq(f"-p ${dynamicCP.mkString(":")}",
        "-m com.futurice.dcolossus.ExampleContext",
        "-i")

      (runner in run).value.run(mainClass, staticCP, args, streams.value.log)
    }

  )
