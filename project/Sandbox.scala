import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.{PlayFilters, PlayScala}
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt._

object Sandbox {
  val projectId = "sandbox"
  val projectName = "Sandbox"
  val projectPort = 20001

  object Versions {
    val app = "1.1.1-SNAPSHOT"
    val scala = "2.12.8"
  }

  val sandbox = Project(id = projectId, base = file(projectId)).settings(Common.settings: _*).settings(
    RoutesKeys.routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport ++= Seq("com.kyleu.projectile.web.util.QueryStringUtils._"),
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playDefaultPort := projectPort,
    PlayKeys.playInteractionMode := PlayUtils.NonBlockingInteractionMode,

    (sourceGenerators in Compile) += ProjectVersion.writeConfig(projectId, projectName, projectPort).taskValue
  ).disablePlugins(PlayFilters).aggregate(LibraryProjects.`projectile-lib-admin`).dependsOn(LibraryProjects.`projectile-lib-admin`).enablePlugins(
    SbtWeb, PlayScala
  )
}
