import sbt._
import sbt.Keys._

object SbtExportPlugin {
  lazy val `sbt-plugin` = (project in file("sbt-plugin")).settings(Shared.commonSettings: _*).settings(
    name := "sbt-plugin",
    sbtPlugin := true
  ).dependsOn(ProjectExport.`project-export`)
}
