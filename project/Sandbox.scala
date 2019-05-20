import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import play.sbt.{PlayFilters, PlayScala}
import sbt.Keys._
import sbt._
import webscalajs.WebScalaJS.autoImport._

object Sandbox {
  val projectId = "sandbox"
  val projectName = "Sandbox"
  val projectPort = 20001

  object Versions {
    val app = "1.1.1-SNAPSHOT"
    val scala = "2.12.8"
  }

  val sandbox = Project(id = projectId, base = file(projectId)).settings(Common.settings: _*).settings(
    RoutesKeys.routesImport ++= Seq("com.kyleu.projectile.models.web.QueryStringUtils._"),
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playDefaultPort := projectPort,
    PlayKeys.playInteractionMode := PlayUtils.NonBlockingInteractionMode,
    RoutesKeys.routesImport += "models.module.ModelBindables._",

    scalaJSProjects := Seq(LibraryProjects.`projectile-lib-scalajs`),

    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages in Assets := Seq(scalaJSPipeline),

    (sourceGenerators in Compile) += ProjectVersion.writeConfig(projectId, projectName, projectPort).taskValue
  ).disablePlugins(PlayFilters).enablePlugins(SbtWeb, PlayScala).dependsOn(
    LibraryProjects.`projectile-lib-admin`,
    ProjectileExport.`projectile-export`
  )
}
