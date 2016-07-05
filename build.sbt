val scalaMajorVersion = "2.10"
val scalaMinorVersion = "6"
val sparkVersion = "1.6.0"

scalaVersion := f"${scalaMajorVersion}.${scalaMinorVersion}"

version := "0.1"

connectInput      in Test := true
parallelExecution in Test := false

libraryDependencies ++= Seq(
  "com.tumblr" %% "colossus" % "0.8.0",
  "fi.veikkaus" %% "dcontext" % "0.2-SNAPSHOT",
  "com.futurice" %% "testtoys" % "0.1-SNAPSHOT" % "test"
)

lazy val testsh =
  taskKey[Unit]("interactive shell for running tasks in a JVM instance, while code may be modified")

lazy val root = (project in file(".")).
  settings(
    name := "dcolossus",
    organization := "fi.futurice",
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
        "-m fi.futurice.dcolossus.ExampleContext",
        "-i")

      (runner in run).value.run(mainClass, staticCP, args, streams.value.log)
    }

  )
