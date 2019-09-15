import sbtcrossproject.CrossPlugin.autoImport.crossProject

val projectVersion = "0.0.1"

val useLatest = false
val profilingEnabled = false

val commonSettings = Seq(version := projectVersion) ++ projectSettings(profilingEnabled = profilingEnabled, useLatest = useLatest)

lazy val shared = projectileCrossProject(crossProject(JSPlatform, JVMPlatform), "shared").settings(commonSettings)

lazy val client = project.in(file("client")).settings(commonSettings).dependsOn(shared.js).enablePlugins(ProjectileScalaJSProject)

lazy val _PROJECT_NAME = Project(id = "_PROJECT_NAME", base = file(".")).settings(commonSettings).settings(
  projectileProjectTitle := "_PROJECT_NAME",
  projectileProjectPort := 9000,

  play.sbt.routes.RoutesKeys.routesImport += "models.module.ModelBindables._",

  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline)
).enablePlugins(ProjectilePlayProject).dependsOn(shared.jvm)
