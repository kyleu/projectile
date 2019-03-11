package com.kyleu.projectile.web.controllers.project

import better.files.File
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.input.TypeScriptInput
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.input.TypeScriptInputService
import com.kyleu.projectile.services.project.ProjectExampleService
import com.kyleu.projectile.services.typescript.{AstExportService, FileService}
import com.kyleu.projectile.util.{Logging, NumberUtils}
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.CollectionUtils
import com.kyleu.projectile.web.views.html.input.ts._

import scala.concurrent.Future

@javax.inject.Singleton
class TypeScriptProjectController @javax.inject.Inject() () extends ProjectileController with Logging {
  private[this] def root = projectile.rootCfg.workingDirectory
  private[this] val srcDir = "tmp/typescript"
  private[this] val tgtDir = "examples/test-typescript-projects"
  private[this] def cache = projectile.rootCfg.configDirectory / ".cache" / "typescript"

  def kids(d: File) = d.children.filter(_.isDirectory).map(c => c.name).filterNot(x => x == "target" || x == "project").toList.sorted

  def sync() = Action.async { implicit request =>
    val root = File(tgtDir)
    val rootKids = kids(root)

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
      val catKids = kids(root / cat)
      val catFile = root / cat / "build.sbt"

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

    Future.successful(Redirect(com.kyleu.projectile.web.controllers.input.routes.TypeScriptController.listRoot()).flashing("success" -> "Synced!"))
  }

  def export(k: String, f: String) = Action.async { implicit request =>
    val (out, logs) = exportProject(k = k, f = f, v = true)
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.outputResults(projectile, Seq(out), logs, verbose = true)))
  }
  def exportAll(k: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val candidates = FileService.kids(root, root / srcDir / k)
    log.info(s"Exporting [${candidates.size}] projects...")
    def statusLog(i: Int, inProgress: Set[(String, String, File)]) = if (i % 10 == 0) {
      val progress = if (inProgress.isEmpty) { "" } else { s". In progress: [${inProgress.map(x => x._1).toSeq.sorted.mkString(", ")}]" }
      log.info(s"Completed exporting [$i / ${candidates.size}] projects" + progress)
    }
    def iter(x: (String, String, File)) = x._1 -> Right(exportProject(k = k, f = x._1, v = false)._1)
    val files = CollectionUtils.parLoop(candidates, iter, statusLog, Some((x: (String, String, File), ex: Throwable) => {
      x._1 -> Left(ex)
    }))
    val status = s"Completed exporting [${candidates.size}] projects in [${NumberUtils.withCommas(System.currentTimeMillis - startMs)}ms]"
    log.info(status)
    Future.successful(Ok(tsBatchExport(projectile, k, status, files)))
  }

  private[this] def exportProject(k: String, f: String, v: Boolean) = {
    val in = getInput(k, f)
    val path = TypeScriptInput.stripName(s"$tgtDir/$k/$f")
    val name = TypeScriptInput.stripName(s"typescript-$k-$f")
    val projectDir = File(path)
    if (!projectDir.exists) {
      projectDir.createIfNotExists(asDirectory = true, createParents = true)
      ProjectExampleService.extract("scalajs", projectDir, name)
    }
    val cfg = new ConfigService(path)
    projectile.exportProjectFromInput(p = in.fakeSummary(), i = in, cfg = cfg, v: Boolean)
  }

  private[this] def getInput(k: String, f: String, compile: Boolean = false) = {
    val tmpKey = srcDir + "/" + k + "/" + f
    val tmp = root / tmpKey
    val key = if (tmp.isDirectory) { tmpKey + "/index.d.ts" } else { tmpKey }
    val file = root / key

    if (file.exists && file.isRegularFile && file.isReadable) {
      if (compile) {
        AstExportService.compile(root = root, f = key, out = cache / key.replaceAllLiterally(".ts", ".json"))
      }
      TypeScriptInputService.inputFor(cfg = projectile.rootCfg, key = f, desc = "", files = Seq(key))
    } else {
      throw new IllegalStateException(s"Cannot read file [${file.pathAsString}]")
    }
  }
}
