package com.kyleu.projectile.sbt

import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import webscalajs.ScalaJSWeb
import com.kyleu.projectile.sbt.SbtProjectile.autoImport.projectileVersion

object ProjectileScalaJSProject extends AutoPlugin {
  override def requires = ScalaJSPlugin && ScalaJSWeb

  lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
    libraryDependencies ++= Seq("com.kyleu" %%% "projectile-lib-scalajs" % projectileVersion)
  )

  override lazy val projectSettings = baseSettings
}
