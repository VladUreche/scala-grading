val commonSettings = Seq(
  organization := "ch.epfl.lamp",
  version := "0.2-SNAPSHOT",
  scalaVersion := "2.11.5",
  crossScalaVersions := Seq("2.10.4", "2.11.5"),
  scalacOptions ++= Seq("-deprecation", "-feature"),

  // Java compiler interface settings
  javacOptions ++= DefaultOptions.javac,
  javacOptions in (Compile) ++= Seq("-source", "1.6"),
  javacOptions in (Compile, compile) ++= Seq("-target", "1.6"),

  homepage := Some(url("https://github.com/sbt-coursera/scala-grading")),
  licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause")),
  organizationHomepage := Some(url("http://lamp.epfl.ch")),

  scmInfo := Some(ScmInfo(
      url("https://github.com/sbt-coursera/scala-grading"),
      "scm:git:git@github.com:sbt-coursera/scala-grading.git",
      Some("scm:git:git@github.com:sbt-coursera/scala-grading.git"))),

  pomExtra := (
    <developers>
      <developer>
        <id>vjovanov</id>
        <name>Vojin Jovanovic</name>
        <url>https://github.com/vjovanov</url>
      </developer>
      <developer>
        <id>sjrd</id>
        <name>Sébastien Doeraene</name>
        <url>https://github.com/sjrd/</url>
      </developer>
    </developers>),

  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

lazy val root = project.in(file(".")).
  settings(
    publishTo := None,
    publish := {},
    publishLocal := {}
  ).
  aggregate(`scala-grading-runtime`, `scala-grading-instragent`)

lazy val `scala-grading-runtime` = project.in(file("runtime")).
  settings(commonSettings: _*).
  settings(
    description := "Runtime library for scala-grading.",

    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1"
  )

lazy val `scala-grading-instragent` = project.in(file("instragent")).
  settings(commonSettings: _*).
  settings(
    description := "Java agent performing instrumentation for scala-grading.",

    autoScalaLibrary := false,
    crossPaths := false,

    libraryDependencies += "org.ow2.asm" % "asm" % "4.0",

    packageOptions += {
      val classpath = (managedClasspath in Compile).value
      val fileStringList = classpath collect {
        case Attributed(file) if file.getName == "asm-4.0.jar" =>
          file.getAbsolutePath()
      }
      val asmJarString = fileStringList.headOption.getOrElse {
        sys.error("could not find asm-4.0.jar on the compile classpath " +
            "of the 'instragent' project: " + classpath)
      }
      Package.ManifestAttributes(
        new java.util.jar.Attributes.Name(
            "Premain-Class")   -> "ch.epfl.lamp.instragent.ProfilingAgent",
        new java.util.jar.Attributes.Name(
            "Boot-Class-Path") -> asmJarString)
    }
  )
