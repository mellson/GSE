organization  := "dk.itu.spcl.server"

version       := "0.2"

scalaVersion  := "2.11.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val AkkaVersion = "2.3.7"
  val SprayVersion = "1.3.2"
  Seq(
    "com.typesafe.akka"      %%  "akka-testkit"  % AkkaVersion   % "test",
    "io.spray"               %%  "spray-testkit" % SprayVersion  % "test",
    "org.specs2"             %%  "specs2-core"   % "2.4.11"      % "test",
    "io.spray"               %%  "spray-can"     % SprayVersion,
    "io.spray"               %%  "spray-routing" % SprayVersion,
    "com.typesafe.akka"      %%  "akka-actor"    % AkkaVersion,
    "com.typesafe.akka"      %%  "akka-slf4j"    % AkkaVersion,
    "io.spray"               %%  "spray-json"    % "1.3.1",
    "com.github.t3hnar"      %%  "scala-bcrypt"  % "2.4",
    "com.github.nscala-time" %%  "nscala-time"   % "1.4.0",
    "org.json4s"             %%  "json4s-native" % "3.2.11"
  )
}
