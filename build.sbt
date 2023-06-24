val Http4sVersion = "0.23.20"
val CirceVersion = "0.14.5"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.8"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "chatbot",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.10",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.9.1",
      "com.lihaoyi" %% "ujson" % "3.1.0",
      "com.lihaoyi" %% "requests" % "0.8.0", // sbt
      "org.scalameta" %% "svm-subs" % "20.2.0",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.9.0",
      "org.telegram" % "telegrambots" % "6.7.0"
    ),
    //    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    //    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
    //    scalacOptions += "-Xlint:help"
  )
