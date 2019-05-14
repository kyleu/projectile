import sbt.Keys._
import sbt._
import sbt.plugins.SbtPlugin

object SbtExportPlugin {
  lazy val `projectile-sbt` = (project in file("projectile-sbt")).settings(Common.settings: _*).settings(
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.21"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9"),
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(
      projectId = Common.projectId + "-sbt",
      projectName = Common.projectName + " SBT Plugin",
      projectPort = 0,
      pkg = "com.kyleu.projectile.sbt.util"
    ).taskValue
  ).enablePlugins(SbtPlugin).dependsOn(ProjectileExport.`projectile-export`).disablePlugins(sbtassembly.AssemblyPlugin)
}
