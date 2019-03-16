import com.kyleu.projectile.sbt.SbtProjectile
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import Dependencies._
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import webscalajs.WebScalaJS.autoImport._
import sbt.Keys._
import sbt._

object Server {
  private[this] val dependencies = Projectile.all ++ PlayFramework.all

  private[this] lazy val serverSettings = Common.commonSettings ++ Seq(
    name := Common.projectId,

    libraryDependencies ++= dependencies,

    // Play
    RoutesKeys.routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport ++= Seq("com.kyleu.projectile.web.util.QueryStringUtils._", "util.web.ModelBindables._"),
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playDefaultPort := Common.projectPort,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages ++= Seq(gzip),

    // Scala.js
    scalaJSProjects := Seq(Client.client)
  )

  lazy val server = Project(id = Common.projectId, base = file(".")).withId(Common.projectId).enablePlugins(
    SbtProjectile, SbtWeb, play.sbt.PlayScala
  ).settings(serverSettings: _*).dependsOn(Shared.sharedJvm)
}
