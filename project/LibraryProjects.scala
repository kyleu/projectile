import sbt.Keys._
import sbt._
import Dependencies._

import org.scalajs.sbtplugin.ScalaJSPlugin
import webscalajs.ScalaJSWeb

object LibraryProjects {
  lazy val `projectile-lib-scala` = (project in file("libraries/projectile-lib-scala")).settings(Common.settings: _*).settings(
    description := "Common classes used by code generated from Projectile",
    libraryDependencies ++= Serialization.all ++ Seq(Utils.enumeratum, Utils.slf4j, Utils.commonsCodec),
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Common.projectId, Common.projectName, Common.projectPort).taskValue
  )

  lazy val `projectile-lib-tracing` = (project in file("libraries/projectile-lib-tracing")).settings(Common.settings: _*).settings(
    description := "Common OpenTracing classes used by code generated from Projectile",
    libraryDependencies ++= Seq(
      Metrics.micrometerCore, Metrics.micrometerStatsd, Metrics.micrometerPrometheus,
      Tracing.datadogTracing, Tracing.jaegerCore, Tracing.jaegerThrift, Tracing.jaegerMetrics,
      Utils.javaxInject, Utils.typesafeConfig
    )
  ).dependsOn(`projectile-lib-scala`)

  lazy val `projectile-lib-jdbc` = (project in file("libraries/projectile-lib-jdbc")).settings(Common.settings: _*).settings(
    description := "Common database classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Database.postgres, Database.hikariCp, Utils.commonsCodec, Utils.typesafeConfig)
  ).dependsOn(`projectile-lib-scala`)

  lazy val `projectile-lib-doobie` = (project in file("libraries/projectile-lib-doobie")).settings(Common.settings: _*).settings(
    description := "Common Doobie classes used by code generated from Projectile",
    libraryDependencies ++= Database.Doobie.all
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-slick` = (project in file("libraries/projectile-lib-slick")).settings(Common.settings: _*).settings(
    description := "Common Slick classes used by code generated from Projectile",
    libraryDependencies ++= Database.Slick.all
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-thrift` = (project in file("libraries/projectile-lib-thrift")).settings(Common.settings: _*).settings(
    description := "Common Thrift classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Thrift.core)
  ).dependsOn(`projectile-lib-tracing`)

  lazy val `projectile-lib-service` = (project in file("libraries/projectile-lib-service")).settings(Common.settings: _*).settings(
    description := "Common service classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Utils.csv, Utils.javaxInject, Utils.scalaGuice)
  ).dependsOn(`projectile-lib-jdbc`, `projectile-lib-tracing`)

  lazy val `projectile-lib-graphql` = (project in file("libraries/projectile-lib-graphql")).settings(Common.settings: _*).settings(
    description := "Common GraphQL classes used by code generated from Projectile",
    libraryDependencies ++= Seq(GraphQL.circe, GraphQL.playJson, GraphQL.sangria, Utils.guice)
  ).dependsOn(`projectile-lib-service`)

  lazy val `projectile-lib-scalajs` = (project in file("libraries/projectile-lib-scalajs")).settings(Common.settings: _*).settings(
    description := "Common ScalaJS classes used by code generated from Projectile",
    libraryDependencies ++= {
      import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
      val enumeratum = "com.beachape" %%% "enumeratum-circe" % Utils.enumeratumCirceVersion
      val jQuery = "be.doeraene" %%% "scalajs-jquery" % "0.9.4"
      val javaTime = "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M13"
      val scalatags = "com.lihaoyi" %%% "scalatags" % "0.6.7"
      Serialization.projects.map(c => "io.circe" %%% c % Serialization.version) ++ Seq(jQuery, scalatags, enumeratum, javaTime)
    }
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb)

  lazy val `projectile-lib-play` = (project in file("libraries/projectile-lib-play")).settings(Common.settings: _*).settings(
    description := "Common Play Framework classes used by code generated from Projectile",
    resolvers += Resolver.bintrayRepo("stanch", "maven"),
    libraryDependencies ++= Seq(Play.lib, Utils.reftree, play.sbt.PlayImport.ws)
  ).dependsOn(`projectile-lib-service`, `projectile-lib-graphql`)

  lazy val `projectile-lib-auth` = (project in file("libraries/projectile-lib-auth")).settings(Common.settings: _*).settings(
    description := "Common Silhouette authentication classes used by code generated from Projectile",
    libraryDependencies ++= Authentication.all ++ WebJars.all
  ).dependsOn(`projectile-lib-play`)

  lazy val all = Seq(
    `projectile-lib-scala`, `projectile-lib-tracing`, `projectile-lib-thrift`,
    `projectile-lib-jdbc`, `projectile-lib-doobie`, `projectile-lib-slick`,
    `projectile-lib-service`, `projectile-lib-graphql`, `projectile-lib-scalajs`, `projectile-lib-play`, `projectile-lib-auth`
  )

  lazy val allReferences = all.map(_.project)
}
