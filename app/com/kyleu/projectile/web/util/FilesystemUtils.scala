package com.kyleu.projectile.web.util

import better.files.File

object FilesystemUtils {
  val tgtDir = "out/typescript-projects"

  private[this] def kids(d: File) = d.children.filter(_.isDirectory).map(c => c.name).filterNot(x => x == "target" || x == "project").toList.sorted

  def syncBuildFiles() = {
    val root = File(tgtDir)
    val rootKids = kids(root)

    saveProjectFiles(root)

    val rootFile = root / "build.sbt"
    val rootContent = Seq("import sbt._", "") ++ rootKids.map(k => s"""lazy val `$k` = project.in(file("$k"))""") ++ Seq(
      "",
      s"lazy val all = Seq(${rootKids.map(x => s"`$x`").mkString(", ")})",
      "",
      "lazy val `aggregate` = {",
      "  val `aggregate` = project.in(file(\".\"))",
      "  all.foldLeft(`aggregate`)((l, r) => l.dependsOn(r).aggregate(r))",
      "}"
    )
    rootFile.overwrite(rootContent.mkString("\n"))

    rootKids.foreach { cat =>
      val catDir = root / cat
      val catKids = kids(catDir)
      val catFile = catDir / "build.sbt"

      saveProjectFiles(catDir)

      val catContent = Seq("import sbt._", "") ++ catKids.map(k => s"""lazy val `$k` = project.in(file("$k"))""") ++ Seq(
        "",
        s"lazy val all = Seq(${catKids.map(x => s"`$x`").mkString(", ")})",
        "",
        s"lazy val `$cat` = {",
        s"""  val `$cat` = project.in(file("."))""",
        s"  all.foldLeft(`$cat`)((l, r) => l.dependsOn(r).aggregate(r))",
        "}"
      )

      catFile.overwrite(catContent.mkString("\n"))
    }
  }

  private[this] def saveProjectFiles(dir: File) = {
    val versionFile = dir / "project" / "build.properties"
    if (!versionFile.exists) {
      versionFile.createIfNotExists(createParents = true)
      versionFile.overwrite("sbt.version=1.2.8")
    }
    val pluginsFile = dir / "project" / "plugins.sbt"
    if (!pluginsFile.exists) {
      pluginsFile.createIfNotExists(createParents = true)
      pluginsFile.overwrite("""addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.26")""")
    }
  }
}
