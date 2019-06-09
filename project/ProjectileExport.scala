import sbt._
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object ProjectileExport {
  lazy val `projectile-export` = (project in file("projectile-export")).settings(Common.settings: _*).settings(
    description := "Project configuration, export, and code generation from Projectile",
    libraryDependencies ++= Seq(Dependencies.Utils.clist, Dependencies.Utils.clistMacros, Dependencies.Testing.scalaTest),

    test in assembly := {},
    assemblyJarName in assembly := Common.projectId + "-export.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("org", "apache", "commons", "logging", _ @ _*) => MergeStrategy.first
      case PathList("javax", "annotation", _ @ _*) => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case "pom.xml" => MergeStrategy.discard
      case "pom.properties" => MergeStrategy.discard
      case x => (assemblyMergeStrategy in assembly).value(x)
    }
  ).dependsOn(
    ParserProjects.`projectile-parser-database`,
    ParserProjects.`projectile-parser-graphql`,
    ParserProjects.`projectile-parser-thrift`,
    ParserProjects.`projectile-parser-typescript`
  )
}
