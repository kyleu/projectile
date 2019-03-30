import Dependencies._
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.SbtWeb.autoImport.Assets
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._

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
      // val magnolia = "io.circe" %%% "circe-magnolia-derivation" % "0.4.0"
      val boopickle = "me.chrons" %%% "boopickle" % Utils.booPickleVersion
      Serialization.projects.map(c => "io.circe" %%% c % Serialization.version) :+ enumeratum /* :+ magnolia */ :+ boopickle
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
    libraryDependencies ++= Thrift.all
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
      val jQuery = "be.doeraene" %%% "scalajs-jquery" % "0.9.4"
      val javaTime = "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M13"
      val jsDom = "org.scala-js" %%% "scalajs-dom" % "0.9.6"
      Seq(jQuery, javaTime, jsDom)
    }
  ).dependsOn(`projectile-lib-core-js`).enablePlugins(org.scalajs.sbtplugin.ScalaJSPlugin, webscalajs.ScalaJSWeb)

  lazy val `projectile-lib-play` = libraryProject(project in file("libraries/projectile-lib-play")).settings(
    description := "Common Play Framework classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Utils.commonsLang, Utils.reftree, play.sbt.PlayImport.ws) ++ WebJars.all
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-service`)

  lazy val `projectile-lib-admin` = libraryProject(project in file("libraries/projectile-lib-admin")).settings(
    description := "A full-featured admin web app with a lovely UI",
    libraryDependencies ++= Authentication.all :+ play.sbt.PlayImport.ehcache,
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-graphql`, `projectile-lib-web-components`)

  lazy val `projectile-lib-auth` = libraryProject(project in file("libraries/projectile-lib-auth")).settings(
    description := "Common Silhouette authentication classes used by code generated from Projectile",
    libraryDependencies ++= Authentication.all :+ play.sbt.PlayImport.ehcache,
    includeFilter in (Assets, LessKeys.less) := "projectile.less"
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-play`)

  lazy val `projectile-lib-auth-graphql` = libraryProject(project in file("libraries/projectile-lib-auth-graphql")).settings(
    description := "Secure GraphQL controllers and views, including GraphiQL and GraphQL Voyager",
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-graphql`, `projectile-lib-auth`)

  lazy val `projectile-lib-websocket` = libraryProject(project in file("libraries/projectile-lib-websocket")).settings(
    description := "Websocket controller and admin actions for actor-backed websocket connections"
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-auth-graphql`)

  lazy val `projectile-lib-web-components` = libraryProject(project in file("libraries/projectile-lib-web-components")).settings(
    description := "Twirl templates for common Material Design web components",
    libraryDependencies += WebJars.materialIcons
  ).enablePlugins(play.sbt.PlayScala).dependsOn(`projectile-lib-play`)

  lazy val all = Seq(
    `projectile-lib-core-jvm`, `projectile-lib-core-js`,
    `projectile-lib-scala`, `projectile-lib-tracing`, `projectile-lib-thrift`,
    `projectile-lib-jdbc`, `projectile-lib-doobie`, `projectile-lib-slick`,
    `projectile-lib-service`, `projectile-lib-graphql`, `projectile-lib-scalajs`,
    `projectile-lib-play`, `projectile-lib-admin`, `projectile-lib-auth`, `projectile-lib-auth-graphql`,
    `projectile-lib-websocket`, `projectile-lib-web-components`
  )

  lazy val allReferences = all.map(_.project)
}
