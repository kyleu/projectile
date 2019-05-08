import com.typesafe.sbt.web.SbtWeb
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
    RoutesKeys.routesImport ++= Seq("com.kyleu.projectile.models.web.QueryStringUtils._"),
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playDefaultPort := projectPort,
    PlayKeys.playInteractionMode := PlayUtils.NonBlockingInteractionMode,

    // libraryDependencies += "com.kyleu" %% "projectile-lib-admin" % Common.Versions.app,

    (sourceGenerators in Compile) += ProjectVersion.writeConfig(projectId, projectName, projectPort).taskValue
  ).disablePlugins(PlayFilters).enablePlugins(SbtWeb, PlayScala).dependsOn(
    LibraryProjects.`projectile-lib-admin`
  )
}
