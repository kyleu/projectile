scapegoatVersion in ThisBuild := Dependencies.Utils.scapegoatVersion

useGpg := true

pgpSecretRing := file("/Users/kyle/.gnupg/pubring.kbx")

lazy val doc = Documentation.doc

lazy val `projectile-lib-scala` = LibraryProjects.`projectile-lib-scala`

lazy val `projectile-export` = ProjectExport.`projectile-export`

lazy val `projectile-sbt` = SbtExportPlugin.`projectile-sbt`

lazy val `projectile-server` = Server.`projectile-server`