import sbt.Keys._
import sbt._
import sbt.plugins.SbtPlugin

object SbtExportPlugin {
  lazy val `projectile-sbt` = (project in file("projectile-sbt")).settings(Common.settings: _*).enablePlugins(
    SbtPlugin
  ).dependsOn(ProjectileExport.`projectile-export`).disablePlugins(sbtassembly.AssemblyPlugin)

  lazy val `projectile-sbt-admin` = (project in file("projectile-sbt-admin")).settings(Common.settings: _*).settings(
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.3"),

    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10"),

    addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1"),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.28"),
    addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.9-0.6" exclude ("org.scala-js", "sbt-scalajs")),

    (sourceGenerators in Compile) += ProjectVersion.writeConfig(
      projectId = Common.projectId + "-sbt",
      projectName = Common.projectName + " SBT Plugin",
      projectPort = 0,
      pkg = "com.kyleu.projectile.sbt.util"
    ).taskValue
  ).enablePlugins(SbtPlugin).dependsOn(`projectile-sbt`).disablePlugins(sbtassembly.AssemblyPlugin)

  lazy val all = if(Common.useLatest) { Nil } else { Seq(`projectile-sbt`, `projectile-sbt-admin`) }

  lazy val allReferences = all.map(_.project)
}
