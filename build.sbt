name := "server"

version := "1.0-SNAPSHOT"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
  "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
)

libraryDependencies ++= Seq(
  cache,
  "org.reactivemongo" %% "reactivemongo" % "0.10.0-SNAPSHOT",
  "play-autosource"   %% "reactivemongo" % "1.0-SNAPSHOT"
)

play.Project.playScalaSettings
