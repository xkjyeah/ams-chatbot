val Http4sVersion = "0.23.20"
val CirceVersion = "0.14.5"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.8"
val MunitCatsEffectVersion = "1.0.7"

lazy val commonSettings = Seq(
  organization := "com.example",
  name := "chatbot",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.10",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "cask" % "0.9.1",
    "com.lihaoyi" %% "ujson" % "3.1.0",
    "com.lihaoyi" %% "requests" % "0.8.0", // sbt
    "org.scalameta" %% "svm-subs" % "20.2.0",
    // The Scala driver by default wants to use streams
    // and be highly reactive. But that would just make our
    // code really, really hard to read.
    // Besides, the Scala driver is just a thin wrapper
    // around the reactiveStreams driver, which makes
    // reading the documentation very hard too
    // So i'm just going to use the Java sync driver.
    //    "org.mongodb.scala" %% "mongo-scala-driver" % "4.10.2",
    "org.mongodb" % "mongodb-driver-sync" % "4.10.2",
    "org.telegram" % "telegrambots" % "6.7.0",
    "org.scala-lang" % "scala-compiler" % "2.13.10",
    // Test
    "org.scalameta" %% "munit" % "0.7.29" % Test
  ),
  //    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
  //    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  testFrameworks += new TestFramework("munit.Framework")
  //    scalacOptions += "-Xlint:help"
)

lazy val root = (project in file("."))
  .dependsOn(macroSub)
  .settings(commonSettings)

lazy val scalaReflect = Def.setting {
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
}

lazy val macroSub = (project in file("macro"))
  .settings(
    commonSettings,
    libraryDependencies += scalaReflect.value
  )

lazy val sandbox = (project in file("sandbox"))
  .dependsOn(macroSub)
  .settings(
    commonSettings,
    libraryDependencies += scalaReflect.value
  )

lazy val web = (project in file("web"))
  .dependsOn(root)
  .settings(
    commonSettings
  )