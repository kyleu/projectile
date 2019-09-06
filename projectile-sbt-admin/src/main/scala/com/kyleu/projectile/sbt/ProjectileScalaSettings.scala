package com.kyleu.projectile.sbt

import sbt.Keys._
import sbt._

object ProjectileScalaSettings {
  object Versions {
    val scala212 = "2.12.9"
    val scala213 = "2.13.0"
  }

  def projectileLib(k: String) = "com.kyleu" %% s"projectile-lib-$k" % com.kyleu.projectile.sbt.util.Version.version

  private[this] val compileOptionsScala213 = Seq(
    "-target:jvm-1.8", "-encoding", "UTF-8", "-feature", "-deprecation", "-explaintypes", "-feature", "-unchecked",
    "–Xcheck-null", "-Xlint", "-Xcheckinit", "-Yrangepos", "-Ywarn-dead-code", "-Ywarn-numeric-widen"
  )

  private[this] val compileOptionsScala212 = compileOptionsScala213 ++ Seq(
    "-Xfatal-warnings", "-Xfuture", "-Ypartial-unification", "-Yno-adapted-args", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-infer-any"
  )

  private[this] val profileOptions = {
    "-Ystatistics:typer" +: Seq("no-profiledb", "show-profiles", "generate-macro-flamegraph").map(s => s"-P:scalac-profiling:$s")
  }

  val silencerLibraries = Seq(
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % "1.4.2"),
    "com.github.ghik" %% "silencer-lib" % "1.4.2" % Provided
  )

  def silencerOptions(path: String, pathFilters: Seq[String] = Seq(".*html", ".*routes"), messageFilters: Seq[String] = Nil) = {
    Seq(s"-P:silencer:sourceRoots=$path") ++ (
      if (pathFilters.isEmpty) { Nil } else { Seq(s"-P:silencer:pathFilters=${pathFilters.mkString(";")}") }
    ) ++ (if (messageFilters.isEmpty) { Nil } else { Seq(s"-P:silencer:globalFilters=${messageFilters.mkString(";")}") })
  }

  def projectSettings(profilingEnabled: Boolean, useLatest: Boolean) = Seq(
    scalaVersion := (if (useLatest) { Versions.scala213 } else { Versions.scala212 }),
    crossScalaVersions := Seq(Versions.scala212, Versions.scala213),

    scalacOptions ++= (if (useLatest) { compileOptionsScala213 } else { compileOptionsScala212 }) ++ (if (profilingEnabled) { profileOptions } else { Nil }),
    scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    scalacOptions in (Compile, doc) := Seq("-encoding", "UTF-8")
  ) ++ (if (profilingEnabled) { Seq(addCompilerPlugin("ch.epfl.scala" %% "scalac-profiling" % "1.0.0")) } else { Nil })
}
