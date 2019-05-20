import sbt.Keys._
import sbt._
import sbt.plugins.SbtPlugin

object SbtExportPlugin {
  lazy val `projectile-sbt` = (project in file("projectile-sbt")).settings(Common.settings: _*).enablePlugins(
    SbtPlugin
  ).dependsOn(ProjectileExport.`projectile-export`).disablePlugins(sbtassembly.AssemblyPlugin)

  lazy val `projectile-sbt-admin` = (project in file("projectile-sbt-admin")).settings(Common.settings: _*).settings(
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2"),

    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.21"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9"),

    addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0"),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.27"),
    addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6" exclude ("org.scala-js", "sbt-scalajs")),

    (sourceGenerators in Compile) += ProjectVersion.writeConfig(
      projectId = Common.projectId + "-sbt",
      projectName = Common.projectName + " SBT Plugin",
      projectPort = 0,
      pkg = "com.kyleu.projectile.sbt.util"
    ).taskValue
  ).enablePlugins(SbtPlugin).dependsOn(`projectile-sbt`).disablePlugins(sbtassembly.AssemblyPlugin)

  lazy val all = Seq(`projectile-sbt`, `projectile-sbt-admin`)

  lazy val allReferences = all.map(_.project)
}
