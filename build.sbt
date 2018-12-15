scapegoatVersion in ThisBuild := Dependencies.Utils.scapegoatVersion

lazy val doc = Documentation.doc

lazy val `projectile-export` = ProjectExport.`projectile-export`

lazy val `projectile-sbt` = SbtExportPlugin.`projectile-sbt`

lazy val `projectile-server` = Server.`projectile-server`