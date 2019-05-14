val projectId = "projectile-admin-example"
val projectVersion = "0.0.1"

lazy val client = project.in(file("client")).settings(Seq(
  version := projectVersion,
  libraryDependencies ++= Seq("com.kyleu" %%% "projectile-lib-scalajs" % projectileVersion)
)).enablePlugins(ScalaJSPlugin, ScalaJSWeb)

lazy val `projectile-admin-example` = Project(id = projectId, base = file(".")).settings(
  projectileProjectId := projectId,
  projectileProjectName := "Example",
  projectileProjectPort := 9000,
  version := projectVersion,

  scalaJSProjects := Seq(client),

  JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
  pipelineStages in Assets := Seq(scalaJSPipeline)
).enablePlugins(ProjectilePlayProject)
