organization  := "dk.itu.spcl.server"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val AkkaVersion = "2.3.5"
  val SprayVersion = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-can"     % SprayVersion,
    "io.spray"            %%  "spray-routing" % SprayVersion,
    "io.spray"            %%  "spray-json"    % "1.2.6",
    "io.spray"            %%  "spray-testkit" % SprayVersion  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % AkkaVersion,
    "com.typesafe.akka"   %%  "akka-testkit"  % AkkaVersion   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

Revolver.settings
