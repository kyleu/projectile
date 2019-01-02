scapegoatVersion in ThisBuild := Dependencies.Utils.scapegoatVersion

useGpg := true

pgpSecretRing := file("/Users/kyle/.gnupg/pubring.kbx")

lazy val doc = Documentation.doc

lazy val `projectile-lib-scala` = LibraryProjects.`projectile-lib-scala`
lazy val `projectile-lib-tracing` = LibraryProjects.`projectile-lib-tracing`
lazy val `projectile-lib-jdbc` = LibraryProjects.`projectile-lib-jdbc`
lazy val `projectile-lib-doobie` = LibraryProjects.`projectile-lib-doobie`
lazy val `projectile-lib-slick` = LibraryProjects.`projectile-lib-slick`
lazy val `projectile-lib-service` = LibraryProjects.`projectile-lib-service`
lazy val `projectile-lib-graphql` = LibraryProjects.`projectile-lib-graphql`
lazy val `projectile-lib-play` = LibraryProjects.`projectile-lib-play`
lazy val `projectile-lib-scalajs` = LibraryProjects.`projectile-lib-scalajs`
lazy val `projectile-lib-auth` = LibraryProjects.`projectile-lib-auth`

lazy val `projectile-export` = ProjectExport.`projectile-export`

lazy val `projectile-sbt` = SbtExportPlugin.`projectile-sbt`

lazy val `projectile-server` = Server.`projectile-server`
