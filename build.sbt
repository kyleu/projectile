scapegoatVersion in ThisBuild := Dependencies.Utils.scapegoatVersion

lazy val doc = Documentation.doc

lazy val `project-export` = ProjectExport.`project-export`

lazy val `sbt-plugin` = SbtExportPlugin.`sbt-plugin`

lazy val server = Server.server
