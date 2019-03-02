import Dependencies._
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin

object ParserProjects {
  lazy val `projectile-parser-core` = (project in file("parsers/projectile-parser-core")).settings(Common.settings: _*).settings(
    libraryDependencies ++= Seq(
      Serialization.jackson, Utils.betterFiles, Utils.chimney, Utils.commonsIo, Utils.commonsLang,
      Utils.enumeratum, Utils.guava, Utils.logging, Utils.thriftParser
    )
  ).disablePlugins(AssemblyPlugin).dependsOn(LibraryProjects.`projectile-lib-scala`)

  lazy val `projectile-parser-database` = (project in file("parsers/projectile-parser-database")).settings(Common.settings: _*).settings(
    description := "Loads schema information from a Postgres database",
    libraryDependencies ++= Seq(Database.postgres),
  ).disablePlugins(AssemblyPlugin).dependsOn(`projectile-parser-core`)

  lazy val `projectile-parser-graphql` = (project in file("parsers/projectile-parser-graphql")).settings(Common.settings: _*).settings(
    description := "Loads GraphQL schema and queries",
    libraryDependencies ++= Seq(GraphQL.sangria, GraphQL.circe),
  ).disablePlugins(AssemblyPlugin).dependsOn(`projectile-parser-core`)

  lazy val `projectile-parser-thrift` = (project in file("parsers/projectile-parser-thrift")).settings(Common.settings: _*).settings(
    description := "Reads and transforms Thrift IDL files",
    libraryDependencies ++= Seq(),
  ).disablePlugins(AssemblyPlugin).dependsOn(`projectile-parser-core`)

  lazy val `projectile-parser-typescript` = (project in file("parsers/projectile-parser-typescript")).settings(Common.settings: _*).settings(
    description := "Loads models from a TypeScript definition files",
    libraryDependencies ++= Seq(),
  ).disablePlugins(AssemblyPlugin).dependsOn(`projectile-parser-core`)

  lazy val all = Seq(
    `projectile-parser-core`, `projectile-parser-database`, `projectile-parser-graphql`, `projectile-parser-thrift`, `projectile-parser-typescript`
  )
  lazy val allReferences = all.map(_.project)
}
