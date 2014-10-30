organization  := "dk.itu.spcl.server"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val AkkaVersion = "2.3.6"
  val SprayVersion = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % SprayVersion,
    "io.spray"            %%  "spray-routing" % SprayVersion,
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "io.spray"            %%  "spray-testkit" % SprayVersion  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % AkkaVersion,
    "com.typesafe.akka"   %%  "akka-testkit"  % AkkaVersion   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.4.9" % "test",
    "com.github.t3hnar"   %%  "scala-bcrypt"  % "2.4"
  )
}

Revolver.settings
