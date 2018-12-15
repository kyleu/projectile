import sbt._
import sbt.Keys._

object SbtExportPlugin {
  lazy val `projectile-sbt` = (project in file("sbt-plugin")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-sbt",
    sbtPlugin := true
  ).dependsOn(ProjectExport.`projectile-export`)
}
