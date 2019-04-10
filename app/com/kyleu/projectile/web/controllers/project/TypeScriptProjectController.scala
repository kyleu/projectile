package com.kyleu.projectile.web.controllers.project

import better.files.File
import com.kyleu.projectile.models.output.{ExportHelper, OutputPackage}
import com.kyleu.projectile.models.typescript.input.TypeScriptInput
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.input.TypeScriptInputService
import com.kyleu.projectile.services.project.ProjectExampleService
import com.kyleu.projectile.services.typescript.{AstExportService, FileService}
import com.kyleu.projectile.util.{Logging, NumberUtils}
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.{AuditUtils, CollectionUtils, FilesystemUtils}
import com.kyleu.projectile.web.views.html.input.ts._

import scala.concurrent.Future

@javax.inject.Singleton
class TypeScriptProjectController @javax.inject.Inject() () extends ProjectileController with Logging {
  private[this] def root = projectile.rootCfg.workingDirectory
  private[this] val srcDir = "tmp/typescript"
  private[this] def cache = projectile.rootCfg.configDirectory / ".cache" / "typescript"

  def kids(d: File) = d.children.filter(_.isDirectory).map(c => c.name).filterNot(x => x == "target" || x == "project").toList.sorted

  def sync() = Action.async { implicit request =>
    FilesystemUtils.syncBuildFiles()
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.input.routes.TypeScriptController.listRoot()).flashing("success" -> "Synced!"))
  }
  def saveAudit(k: String, f: String) = Action.async { implicit request =>
    AuditUtils.saveAudit(k, TypeScriptInput.stripName(f))
    val msg = "success" -> "Saved!"
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.project.routes.TypeScriptProjectController.export(k, f)).flashing(msg))
  }
  def audit() = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val results = AuditUtils.auditAll()
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.auditResults(projectile, results, (System.currentTimeMillis - startMs).toInt)))
  }

  def export(k: String, f: String) = Action.async { implicit request =>
    val (out, logs) = exportProject(k = k, f = f, v = true)
    val header = (key: String) => {
      val url = com.kyleu.projectile.web.controllers.project.routes.TypeScriptProjectController.saveAudit(k, f).url
      play.twirl.api.Html(s"""<div class="right"><a href="$url">Save Audit</a></div>""")
    }
    Future.successful(Ok(com.kyleu.projectile.web.views.html.project.outputResults(projectile, header, Seq(out), logs, verbose = true)))
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
    val p = in.fakeSummary().copy(packages = Map(OutputPackage.Application -> Seq("com", "definitelyscala", ExportHelper.escapeKeyword(k))))

    val path = TypeScriptInput.stripName(s"${FilesystemUtils.tgtDir}/$k/$f")
    val name = TypeScriptInput.stripName(s"$k-$f")
    val projectDir = File(path)
    if (!projectDir.exists) {
      projectDir.createIfNotExists(asDirectory = true, createParents = true)
      ProjectExampleService.extract("scalajs", projectDir, name)
    }
    val cfg = new ConfigService(path)
    projectile.exportProjectFromInput(p = p, i = in, cfg = cfg, v: Boolean)
  }

  private[this] def getInput(k: String, f: String, compile: Boolean = false) = {
    val tmpKey = srcDir + "/" + k + "/" + f
    val tmp = root / tmpKey
    val key = if (tmp.isDirectory) { tmpKey + "/index.d.ts" } else if (tmp.exists) { tmpKey } else { tmpKey + ".d.ts" }
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
