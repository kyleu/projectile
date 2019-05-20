val projectVersion = "0.0.1"

lazy val client = project.in(file("client")).settings(Seq(version := projectVersion)).enablePlugins(ProjectileScalaJSProject)

lazy val _PROJECT_NAME = Project(id = "_PROJECT_NAME", base = file(".")).settings(
  projectileProjectTitle := "_PROJECT_NAME",
  projectileProjectPort := 9000,
  version := projectVersion,

  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline)
).enablePlugins(ProjectilePlayProject)
