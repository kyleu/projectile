import sbt.Keys._
import sbt._
import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtassembly.AssemblyPlugin

object LibraryProjects {
  private[this] def libraryProject[T <: Project](p: T) = {
    p.settings(Common.settings: _*).disablePlugins(AssemblyPlugin)
  }

  private[this] lazy val `projectile-lib-core` = crossProject(JSPlatform, JVMPlatform).withoutSuffixFor(JVMPlatform).crossType(CrossType.Pure).in(
    file("libraries/projectile-lib-core")
  ).settings(Common.settings: _*).settings(
    description := "Classes and utilities shared between Scala and Scala.js",
    libraryDependencies ++= {
      val enumeratum = "com.beachape" %%% "enumeratum-circe" % Utils.enumeratumCirceVersion
      Serialization.projects.map(c => "io.circe" %%% c % Serialization.version) :+ enumeratum
    },
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Common.projectId, Common.projectName, Common.projectPort).taskValue
  ).jsSettings(libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M13").disablePlugins(AssemblyPlugin)

  lazy val `projectile-lib-core-jvm` = `projectile-lib-core`.jvm.withId("projectile-lib-core")
  lazy val `projectile-lib-core-js` = `projectile-lib-core`.js.withId("projectile-lib-core-js")

  lazy val `projectile-lib-scala` = libraryProject(project in file("libraries/projectile-lib-scala")).settings(
    description := "Common classes relating to core models and utilities",
    libraryDependencies ++= Seq(Utils.slf4j, Utils.commonsCodec),
  ).dependsOn(`projectile-lib-core-jvm`)

  lazy val `projectile-lib-tracing` = libraryProject(project in file("libraries/projectile-lib-tracing")).settings(
    description := "Common OpenTracing classes used by code generated from Projectile",
    libraryDependencies ++= Seq(
      Metrics.micrometerCore, Metrics.micrometerStatsd, Metrics.micrometerPrometheus,
      Tracing.datadogTracing, Tracing.jaegerCore, Tracing.jaegerThrift, Tracing.jaegerMetrics,
      Utils.javaxInject, Utils.typesafeConfig
    )
  ).dependsOn(`projectile-lib-scala`)

  lazy val `projectile-lib-jdbc` = libraryProject(project in file("libraries/projectile-lib-jdbc")).settings(
    description := "Common database classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Database.postgres, Database.hikariCp, Database.flyway, Utils.commonsCodec, Utils.typesafeConfig)
  ).dependsOn(`projectile-lib-scala`)

  lazy val `projectile-lib-doobie` = libraryProject(project in file("libraries/projectile-lib-doobie")).settings(
    description := "Common Doobie classes used by code generated from Projectile",
    libraryDependencies ++= Database.Doobie.all
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-slick` = libraryProject(project in file("libraries/projectile-lib-slick")).settings(
    description := "Common Slick classes used by code generated from Projectile",
    libraryDependencies ++= Database.Slick.all
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-thrift` = libraryProject(project in file("libraries/projectile-lib-thrift")).settings(
    description := "Common Thrift classes used by code generated from Projectile",
    libraryDependencies += Thrift.core
  ).dependsOn(`projectile-lib-tracing`)

  lazy val `projectile-lib-service` = libraryProject(project in file("libraries/projectile-lib-service")).settings(
    description := "Common service classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Utils.csv, Utils.javaxInject, Utils.scalaGuice)
  ).dependsOn(`projectile-lib-jdbc`, `projectile-lib-tracing`)

  lazy val `projectile-lib-graphql` = libraryProject(project in file("libraries/projectile-lib-graphql")).settings(
    description := "Common GraphQL classes used by code generated from Projectile",
    libraryDependencies ++= Seq(GraphQL.circe, GraphQL.playJson, GraphQL.sangria, Utils.guice)
  ).dependsOn(`projectile-lib-service`)

  lazy val `projectile-lib-scalajs` = libraryProject(project in file("libraries/projectile-lib-scalajs")).settings(
    description := "Common Scala.js classes used by code generated from Projectile",
    libraryDependencies ++= {
      import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
      val enumeratum = "com.beachape" %%% "enumeratum-circe" % Utils.enumeratumCirceVersion
      val jQuery = "be.doeraene" %%% "scalajs-jquery" % "0.9.4"
      val javaTime = "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M13"
      val scalatags = "com.lihaoyi" %%% "scalatags" % "0.6.7"
      Serialization.projects.map(c => "io.circe" %%% c % Serialization.version) ++ Seq(jQuery, scalatags, enumeratum, javaTime)
    }
  ).dependsOn(`projectile-lib-core-js`).enablePlugins(org.scalajs.sbtplugin.ScalaJSPlugin, webscalajs.ScalaJSWeb)

  lazy val `projectile-lib-play` = libraryProject(project in file("libraries/projectile-lib-play")).settings(
    description := "Common Play Framework classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Utils.commonsLang, Utils.reftree, play.sbt.PlayImport.ws) ++ WebJars.all
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-service`)

  lazy val `projectile-lib-websocket` = libraryProject(project in file("libraries/projectile-lib-websocket")).settings(
    description := "Websocket controller and admin actions for actor-backed websocket connections"
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-play`)

  lazy val `projectile-lib-auth` = libraryProject(project in file("libraries/projectile-lib-auth")).settings(
    description := "Common Silhouette authentication classes used by code generated from Projectile",
    libraryDependencies ++= Authentication.all :+ play.sbt.PlayImport.ehcache
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-play`)

  lazy val `projectile-lib-auth-graphql` = libraryProject(project in file("libraries/projectile-lib-auth-graphql")).settings(
    description := "Secure GraphQL controllers and views, including GraphiQL and GraphQL Voyager",
    libraryDependencies ++= Authentication.all :+ play.sbt.PlayImport.ehcache
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-graphql`, `projectile-lib-auth`)

  lazy val all = Seq(
    `projectile-lib-core-jvm`, `projectile-lib-core-js`,
    `projectile-lib-scala`, `projectile-lib-tracing`, `projectile-lib-thrift`,
    `projectile-lib-jdbc`, `projectile-lib-doobie`, `projectile-lib-slick`,
    `projectile-lib-service`, `projectile-lib-graphql`, `projectile-lib-scalajs`,
    `projectile-lib-play`, `projectile-lib-websocket`,
    `projectile-lib-auth`, `projectile-lib-auth-graphql`
  )

  lazy val allReferences = all.map(_.project)
}
