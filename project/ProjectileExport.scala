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
      case PathList("javax", "servlet", _ @ _*) => MergeStrategy.first
      case PathList("javax", "xml", _ @ _*) => MergeStrategy.first
      case PathList(p @ _*) if p.last.contains("about_jetty-") => MergeStrategy.discard
      case PathList("org", "apache", "commons", "logging", _ @ _*) => MergeStrategy.first
      case PathList("org", "w3c", "dom", _ @ _*) => MergeStrategy.first
      case PathList("org", "w3c", "dom", "events", _ @ _*) => MergeStrategy.first
      case PathList("javax", "annotation", _ @ _*) => MergeStrategy.first
      case PathList("net", "jcip", "annotations", _ @ _*) => MergeStrategy.first
      case PathList("sqlj", _ @ _*) => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case "pom.xml" => MergeStrategy.discard
      case "JS_DEPENDENCIES" => MergeStrategy.discard
      case "pom.properties" => MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case x => (assemblyMergeStrategy in assembly).value(x)
    },
  ).dependsOn(
    ParserProjects.`projectile-parser-database`,
    ParserProjects.`projectile-parser-graphql`,
    ParserProjects.`projectile-parser-thrift`,
    ParserProjects.`projectile-parser-typescript`
  )
}
