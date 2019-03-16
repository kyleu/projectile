package com.kyleu.projectile.web.controllers.input

import better.files.File
import com.kyleu.projectile.models.typescript.node.NodeHelper.asString
import com.kyleu.projectile.models.typescript.node.{NodeHelper, TypeScriptNode}
import com.kyleu.projectile.services.input.TypeScriptInputService
import com.kyleu.projectile.services.typescript.{AstExportService, FileService}
import com.kyleu.projectile.util.{Logging, NumberUtils}
import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.web.util.CollectionUtils
import com.kyleu.projectile.web.views.html.input.ts._
import play.twirl.api.Html

import scala.concurrent.Future

object TypeScriptController {
  def viewNode(node: TypeScriptNode, depth: Int): Html = tsNode(node = node, content = Html(asString(node)), depth = depth)

  def nodeDetails(node: TypeScriptNode) = {
    import com.kyleu.projectile.util.JsonSerializers._
    val json = extractObj[io.circe.JsonObject](extract[io.circe.JsonObject](node.asJson), node.getClass.getSimpleName)
    val trimmed = json.filter(_._1 != "ctx").asJson
    val ctx = extractObj[io.circe.JsonObject](json, "ctx").filter(x => x._1 != "src" && x._1 != "json").asJson
    val ast = if (node.ctx.json.isNull) { None } else { Some(node.ctx.json) }
    (trimmed, ctx, ast)
  }

  case class Result(key: String, res: Seq[String], errors: Seq[TypeScriptNode.Error], unknowns: Seq[TypeScriptNode.Unknown])
}

@javax.inject.Singleton
class TypeScriptController @javax.inject.Inject() () extends ProjectileController with Logging {
  private[this] def root = projectile.rootCfg.workingDirectory
  private[this] val dir = "tmp/typescript"
  private[this] def cache = projectile.rootCfg.configDirectory / ".cache" / "typescript"

  def listRoot() = Action.async { implicit request =>
    Future.successful(Ok(tsRoot(projectile, (root / dir).children.filter(_.isDirectory).map(_.name).toList.sorted)))
  }
  def list(k: String) = Action.async { implicit request =>
    Future.successful(Ok(tsList(projectile, k, FileService.kids(root, root / dir / k).map(_._1))))
  }

  def parse(k: String, f: String, compile: Boolean) = Action.async { implicit request =>
    Future.successful(Ok(tsView.apply(projectile = projectile, k = k, path = f, input = getInput(k, f, compile), forceCompile = compile)))
  }
  def parseAll(k: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val candidates = FileService.kids(root, root / dir / k)
    log.info(s"Parsing [${candidates.size}] projects...")
    def statusLog(i: Int, inProgress: Set[(String, String, File)]) = if (i % 100 == 0) {
      val progress = if (inProgress.isEmpty) { "" } else { s". In progress: [${inProgress.map(x => x._1).toSeq.sorted.mkString(", ")}]" }
      log.info(s"Completed parsing [$i / ${candidates.size}] projects" + progress)
    }
    def iter(x: (String, String, File)) = {
      val n = FileService.parseFile(root = root, cache = cache, path = x._2)
      result(k = x._1, msgs = n._1, node = n._2)
    }
    val files = CollectionUtils.parLoop(candidates, iter, statusLog, Some((f: (String, String, File), ex: Throwable) => {
      ex.printStackTrace()
      result(f._1, Nil, TypeScriptNode.Error(kind = f._2, cls = f._3.pathAsString, msg = ex.toString))
    }))
    val status = s"Completed parsing [${candidates.size}] projects in [${NumberUtils.withCommas(System.currentTimeMillis - startMs)}ms]"
    log.info(status)
    Future.successful(Ok(tsBatchParse(projectile, k, status, files)))
  }

  private[this] def getInput(k: String, f: String, compile: Boolean = false) = {
    val tmpKey = dir + "/" + k + "/" + f
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

  private[this] def result(k: String, msgs: Seq[String], node: TypeScriptNode) = {
    TypeScriptController.Result(k, msgs, NodeHelper.getErrorNodes(node), NodeHelper.getUnknownNodes(node))
  }
}
