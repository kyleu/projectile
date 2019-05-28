import sbtcrossproject.CrossPlugin.autoImport.crossProject

val projectVersion = "0.0.1"

lazy val shared = projectileCrossProject(crossProject(JSPlatform, JVMPlatform), "shared").settings(version := projectVersion)

lazy val client = project.in(file("client")).settings(Seq(version := projectVersion)).dependsOn(shared.js).enablePlugins(ProjectileScalaJSProject)

lazy val _PROJECT_NAME = Project(id = "_PROJECT_NAME", base = file(".")).settings(
  projectileProjectTitle := "_PROJECT_NAME",
  projectileProjectPort := 9000,
  version := projectVersion,

  play.sbt.routes.RoutesKeys.routesImport += "models.module.ModelBindables._",

  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline)
).enablePlugins(ProjectilePlayProject).dependsOn(shared.jvm)
