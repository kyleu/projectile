import sbt._
import sbt.Keys._

object SbtExportPlugin {
  lazy val `projectile-sbt` = (project in file("projectile-sbt")).settings(Common.settings: _*).settings(
    sbtPlugin := true
  ).dependsOn(ProjectileExport.`projectile-export`).disablePlugins(sbtassembly.AssemblyPlugin)
}
