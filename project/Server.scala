import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport.{scapegoatIgnoredFiles, scapegoatDisabledInspections}
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object Server {
  private[this] val dependencies = {
    import Dependencies._
    Serialization.all ++ Seq(
      Play.filters, Play.guice, Play.ws, Play.json, Play.cache,
      GraphQL.sangria, GraphQL.playJson, GraphQL.circe,
      WebJars.jquery, WebJars.fontAwesome, WebJars.materialize,
      Utils.scalaGuice, Utils.clistMacros
    )
  }

  private[this] lazy val serverSettings = Common.settings ++ Seq(
    name := Common.projectId,
    description := Common.projectName,

    libraryDependencies ++= dependencies,

    // Play
    RoutesKeys.routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport ++= Seq("com.kyleu.projectile.web.util.QueryStringUtils._"),
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playDefaultPort := Common.projectPort,
    PlayKeys.playInteractionMode := PlayUtils.NonBlockingInteractionMode,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages += gzip,
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,

    // Prevent Scaladoc
    sources in (Compile, doc) := Seq.empty,

    // Source Control
    scmInfo := Some(ScmInfo(url("https://github.com/KyleU/projectile"), "git@github.com:KyleU/projectile.git")),
    git.remoteRepo := scmInfo.value.get.connection,

    // Fat-Jar Assembly
    assemblyJarName in assembly := Common.projectId + ".jar",
    assemblyMergeStrategy in assembly := {
      case "play/reference-overrides.conf" => MergeStrategy.concat
      case x => (assemblyMergeStrategy in assembly).value(x)
    },
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    mainClass in assembly := Some("CLI"),

    // Code Quality
    scapegoatIgnoredFiles := Seq(".*/Routes.scala", ".*/RoutesPrefix.scala", ".*/*ReverseRoutes.scala", ".*/*.template.scala"),
    scapegoatDisabledInspections := Seq("UnusedMethodParameter")
  )


  private[this] def withProjects(project: Project, dependents: Project*) = dependents.foldLeft(project)((l, r) => l.dependsOn(r).aggregate(r))

  lazy val `projectile-server` = withProjects(
    Project(id = Common.projectId, base = file(".")).enablePlugins(
      SbtWeb, play.sbt.PlayScala, diagram.ClassDiagramPlugin
    ).settings(serverSettings: _*),
    SbtExportPlugin.`projectile-sbt`,
    ProjectExport.`projectile-export`
  ).dependsOn(LibraryProjects.`projectile-lib-auth`).aggregate(LibraryProjects.allReferences: _*)
}
