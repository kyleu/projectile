package com.kyleu.projectile.sbt

import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{Docker, dockerExposedPorts, dockerExposedVolumes}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.{Universal, useNativeZip}
import sbt._
import sbt.Keys._
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import play.sbt.PlayScala
import sbtassembly.AssemblyPlugin.autoImport._

object ProjectilePlayProject extends AutoPlugin {
  private[this] def isConf(x: (File, String)) = x._1.getAbsolutePath.contains("conf/")

  object autoImport {
    val projectileProjectId = settingKey[String]("The id of your project")
    val projectileProjectName = settingKey[String]("The name of your project")
    val projectileProjectPort = settingKey[Int]("The port for your project")
    val projectileVersion = com.kyleu.projectile.sbt.util.Version.version
    def projectileLib(k: String) = "com.kyleu" %% s"projectile-lib-$k" % projectileVersion
  }

  override def requires = SbtProjectile && PlayScala && JavaAppPackaging && UniversalPlugin && DockerPlugin

  lazy val baseSettings: Seq[Def.Setting[_]] = useNativeZip ++ Seq(
    name := autoImport.projectileProjectId.value,
    description := autoImport.projectileProjectName.value,

    // Projectile
    libraryDependencies += autoImport.projectileLib("admin"),

    // Scala
    scalaVersion := "2.12.8",

    scalacOptions ++= Seq(
      "-target:jvm-1.8", "-encoding", "UTF-8", "-feature", "-deprecation", "-explaintypes", "-feature", "-unchecked",
      "–Xcheck-null", /* "-Xfatal-warnings", */ /* "-Xlint", */ "-Xcheckinit", "-Xfuture", "-Yrangepos", "-Ypartial-unification",
      /* "-Yno-adapted-args", */ "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-numeric-widen", "-Ywarn-infer-any"
    ),

    scalacOptions in (Compile, console) ~= (_.filterNot(Set("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    scalacOptions in (Compile, doc) := Seq("-encoding", "UTF-8"),

    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false),

    // Packaging
    topLevelDirectory in Universal := None,
    packageSummary := description.value,
    packageDescription := description.value,

    mappings in Universal := (mappings in Universal).value.filterNot(isConf),

    // Docker
    dockerExposedPorts := Seq(autoImport.projectileProjectPort.value),
    dockerLabels ++= Map("project" -> autoImport.projectileProjectId.value),
    dockerUpdateLatest := true,
    defaultLinuxInstallLocation in Docker := s"/opt/${autoImport.projectileProjectId.value}",
    packageName in Docker := autoImport.projectileProjectId.value,
    dockerExposedVolumes := Seq(s"/opt/${autoImport.projectileProjectId.value}"),
    version in Docker := version.value,

    // Assembly
    assemblyJarName in assembly := autoImport.projectileProjectId.value + ".jar",
    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", _@ _*) => MergeStrategy.first
      case PathList("javax", "xml", _@ _*) => MergeStrategy.first
      case PathList(p @ _*) if p.last.contains("about_jetty-") => MergeStrategy.discard
      case PathList("org", "apache", "commons", "logging", _@ _*) => MergeStrategy.first
      case PathList("org", "w3c", "dom", _@ _*) => MergeStrategy.first
      case PathList("org", "w3c", "dom", "events", _@ _*) => MergeStrategy.first
      case PathList("javax", "annotation", _@ _*) => MergeStrategy.first
      case PathList("net", "jcip", "annotations", _@ _*) => MergeStrategy.first
      case PathList("play", "api", "libs", "ws", _@ _*) => MergeStrategy.first
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
      case PathList("sqlj", _@ _*) => MergeStrategy.first
      case PathList("play", "reference-overrides.conf") => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case "messages" => MergeStrategy.concat
      case "pom.xml" => MergeStrategy.discard
      case "JS_DEPENDENCIES" => MergeStrategy.discard
      case "pom.properties" => MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case x => (assemblyMergeStrategy in assembly).value(x)
    },

    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),

    // Source generator
    (sourceGenerators in Compile) += Def.taskDyn {
      ProjectVersion.writeConfig(
        projectId = autoImport.projectileProjectId.value,
        projectName = autoImport.projectileProjectName.value,
        projectPort = autoImport.projectileProjectPort.value
      )
    },

    // Play
    RoutesKeys.routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport ++= Seq("com.kyleu.projectile.models.web.QueryStringUtils._"),
    PlayKeys.externalizeResources := false,
    PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite"),
    PlayKeys.playDefaultPort := autoImport.projectileProjectPort.value,
    PlayKeys.playInteractionMode := PlayUtils.NonBlockingInteractionMode,

    // Assets
    packagedArtifacts in publishLocal := {
      val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
      val assets: java.io.File = (PlayKeys.playPackageAssets in Compile).value
      artifacts + (Artifact(moduleName.value, "jar", "jar", "assets") -> assets)
    }
  )

  override lazy val projectSettings = baseSettings
}
