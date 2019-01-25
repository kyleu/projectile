import sbt._
import sbt.Keys._

object ProjectExport {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      GraphQL.sangria, GraphQL.circe, Database.postgres, Serialization.jackson,
      Utils.betterFiles, Utils.chimney, Utils.commonsIo, Utils.commonsLang, Utils.enumeratum,
      Utils.guava, Utils.clist, Utils.clistMacros, Utils.logging, Utils.thriftParser, Testing.scalaTest
    )
  }

  lazy val `projectile-export` = (project in file("projectile-export")).settings(Common.settings: _*).settings(
    description := "Project configuration, export, and code generation from Projectile",
    libraryDependencies ++= dependencies,
  ).dependsOn(LibraryProjects.`projectile-lib-scala`).disablePlugins(sbtassembly.AssemblyPlugin)
}
