import sbt._
import sbt.Keys._

object ProjectExport {
  private[this] val dependencies = {
    import Dependencies._
    Serialization.all ++ Seq(
      GraphQL.sangria, GraphQL.circe, Database.postgres,
      Utils.betterFiles, Utils.chimney, Utils.commonsIo, Utils.commonsLang, Utils.enumeratum,
      Utils.guava, Utils.clist, Utils.clistMacros, Utils.logging, Utils.thriftParser, Testing.scalaTest
    )
  }

  lazy val `projectile-export` = (project in file("project-export")).settings(Common.settings: _*).settings(
    name := "projectile-export",
    description := "Project configuration, export, and code generation from Projectile",
    libraryDependencies ++= dependencies,
  ).dependsOn(LibraryProjects.`projectile-lib-scala`)
}
