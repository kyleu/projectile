import sbt.Keys._
import sbt._
import Dependencies._

object LibraryProjects {
  lazy val `projectile-lib-scala` = (project in file("libraries/projectile-lib-scala")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-scala",
    description := "Common classes used by code generated from Projectile",
    libraryDependencies ++= Serialization.all ++ Seq(Utils.enumeratum, Utils.slf4j, Utils.commonsCodec),
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Shared.projectId, Shared.projectName, Shared.projectPort).taskValue
  )

  lazy val `projectile-lib-jdbc` = (project in file("libraries/projectile-lib-jdbc")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-jdbc",
    description := "Common database classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Database.postgres, Database.hikariCp, Utils.commonsCodec, Utils.typesafeConfig)
  ).dependsOn(`projectile-lib-scala`)

  lazy val `projectile-lib-doobie` = (project in file("libraries/projectile-lib-doobie")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-doobie",
    description := "Common Doobie classes used by code generated from Projectile",
    libraryDependencies ++= Database.Doobie.all
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-slick` = (project in file("libraries/projectile-lib-slick")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-slick",
    description := "Common Slick classes used by code generated from Projectile",
    libraryDependencies ++= Database.Slick.all
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-service` = (project in file("libraries/projectile-lib-service")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-service",
    description := "Common service classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Utils.csv, Utils.javaxInject)
  ).dependsOn(`projectile-lib-jdbc`)

  lazy val `projectile-lib-graphql` = (project in file("libraries/projectile-lib-graphql")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-graphql",
    description := "Common GraphQL classes used by code generated from Projectile",
    libraryDependencies ++= Serialization.all ++ Seq(GraphQL.circe, GraphQL.sangria)
  ).dependsOn(`projectile-lib-service`)

  lazy val `projectile-lib-play` = (project in file("libraries/projectile-lib-play")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-play",
    description := "Common Play Framework classes used by code generated from Projectile",
    libraryDependencies ++= Seq(Play.lib)
  ).dependsOn(`projectile-lib-service`)

  lazy val all: Seq[ProjectReference] = Seq(
    `projectile-lib-scala`, `projectile-lib-jdbc`, `projectile-lib-doobie`, `projectile-lib-slick`,
    `projectile-lib-service`, `projectile-lib-graphql`, `projectile-lib-play`
  )
}
