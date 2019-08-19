import sbt.Keys._
import sbt._

object Common {
  val projectId = "projectile"
  val projectName = "Projectile"
  val projectPort = 20000

  object Versions {
    val app = "1.13.4"
    val scala = "2.12.9"
    // val scala = "2.13.0"
    // Requires:
    // - enumeratum-circe
    // - sangria
  }

  private[this] val profilingEnabled = false
  private[this] val profileOptions = if (profilingEnabled) {
    "-Ystatistics:typer" +: Seq("no-profiledb", "show-profiles", "generate-macro-flamegraph").map(s => s"-P:scalac-profiling:$s")
  } else { Nil }

  val compileOptions = Seq(
    "-target:jvm-1.8", "-encoding", "UTF-8", "-feature", "-deprecation", "-explaintypes", "-feature", "-unchecked",
    /* "-Xfatal-warnings", */ "–Xcheck-null", "-Xlint", "-Xcheckinit", "-Xfuture",
    "-Yrangepos", "-Ypartial-unification", "-Yno-adapted-args", "-Ywarn-dead-code",
    "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-numeric-widen", "-Ywarn-infer-any"
  ) ++ profileOptions

  lazy val settings = Seq(
    version := Common.Versions.app,
    scalaVersion := Common.Versions.scala,
    organization := "com.kyleu",

    licenses := Seq("CC0" -> url("https://creativecommons.org/publicdomain/zero/1.0")),
    homepage := Some(url("https://projectile.kyleu.com")),
    scmInfo := Some(ScmInfo(url("https://github.com/KyleU/projectile"), "scm:git@github.com:KyleU/projectile.git")),
    developers := List(Developer(id = "kyleu", name = "Kyle Unverferth", email = "opensource@kyleu.com", url = url("http://kyleu.com"))),

    scalacOptions ++= compileOptions,
    scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    scalacOptions in (Compile, doc) := Seq("-encoding", "UTF-8"),

    publishMavenStyle := true,

    publishTo := xerial.sbt.Sonatype.SonatypeKeys.sonatypePublishTo.value
  ) ++ (if(profilingEnabled) { Seq(addCompilerPlugin("ch.epfl.scala" %% "scalac-profiling" % "1.0.0")) } else { Nil })

  def silencerOptions(path: String, pathFilters: Seq[String] = Nil, messageFilters: Seq[String] = Nil) = {
    Seq(s"-P:silencer:sourceRoots=$path") ++ (
      if(pathFilters.isEmpty) { Nil } else { Seq(s"-P:silencer:pathFilters=${pathFilters.mkString(";")}") }
    ) ++ (if(messageFilters.isEmpty) { Nil } else { Seq(s"-P:silencer:globalFilters=${messageFilters.mkString(";")}") })
  }
}
