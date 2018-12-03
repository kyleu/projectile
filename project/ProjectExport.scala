import sbt._
import sbt.Keys._

object ProjectExport {
  private[this] val dependencies = {
    import Dependencies._
    Serialization.all ++ Seq(
      GraphQL.sangria, GraphQL.circe, Database.postgres, Testing.scalaTest,
      Utils.betterFiles, Utils.chimney, Utils.commonsIo, Utils.commonsLang, Utils.enumeratum,
      Utils.guava, Utils.clist, Utils.clistMacros, Utils.logging, Utils.thriftParser
    )
  }

  lazy val `project-export` = (project in file("project-export")).settings(Shared.commonSettings: _*).settings(
    name := "project-export",
    description := "Project configuration, export, and code generation from Projectile",
    libraryDependencies ++= dependencies,
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Shared.projectId, Shared.projectName, Shared.projectPort).taskValue,
  )
}
