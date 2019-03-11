package com.kyleu.projectile.services.typescript

import better.files.File
import com.kyleu.projectile.models.typescript.input.TypeScriptInput
import com.kyleu.projectile.models.typescript.node._
import com.kyleu.projectile.util.NumberUtils

object FileService {
  def normalize(root: File, f: File) = {
    val file = f match {
      case _ if f.isRegularFile && f.name.endsWith(".ts") => Some(f)
      case _ if f.isRegularFile && (f.parent / s"${f.name}.d.ts").exists => Some(f.parent / s"${f.name}.d.ts")
      case _ if f.isRegularFile && (f.parent / s"${f.name}.ts").exists => Some(f.parent / s"${f.name}.ts")
      case _ if f.isDirectory && (f / "index.d.ts").exists => Some(f / "index.d.ts")
      case _ => None
    }
    file.map(x => (f.name, root.relativize(x).toString, x))
  }

  def kids(root: File, file: File) = file.children.flatMap(normalize(root, _)).toList.sortBy(_._1)

  def loadTypeScriptInput(root: File, cache: File, files: Seq[String], input: TypeScriptInput) = {
    val newNodes = files.flatMap {
      case f if f.endsWith("/*") => kids(root, root / f.stripSuffix("/*")).map(x => parseFile(root = root, cache = cache, path = x._2))
      case f => Seq(parseFile(root = root, cache = cache, path = f))
    }
    input.copy(nodes = input.nodes ++ newNodes.map(_._2), logs = newNodes.flatMap(_._1))
  }

  def parseFile(root: File, cache: File, path: String, forceCompile: Boolean = false, encountered: Set[String] = Set.empty): (Seq[String], TypeScriptNode) = {
    val (_, norm, _) = normalize(root, root / path).getOrElse(throw new IllegalStateException(s"Cannot load [$path]"))
    val (parseStatus, json) = AstExportService.parseAst(root = root, cache = cache, f = norm, forceCompile = forceCompile)
    val file = root / path match {
      case x if x.isDirectory => x / "index.d.ts"
      case x => x
    }

    if (!file.exists) { throw new IllegalStateException(s"Cannot load non-existent file [${file.pathAsString}]") }
    if (file.isDirectory) { throw new IllegalStateException(s"Cannot load directory [${file.pathAsString}]") }
    if (!file.isReadable) { throw new IllegalStateException(s"Cannot read file [${file.pathAsString}]") }
    if (file.size == 0) { throw new IllegalStateException(s"Cannot read empty file [${file.pathAsString}]") }

    val sourcecode = file.contentAsString
    val startMs = System.currentTimeMillis
    val params = ServiceParams(
      root = root, cache = cache, path = path, sourcecode = sourcecode, depth = 0,
      parseRefs = true, forceCompile = forceCompile, encountered = encountered + path, messages = Nil
    )
    val result = JsonService.parseJson(json = json, params = params)
    val msgs = (parseStatus.toSeq :+ s"Parsed [$path] in [${NumberUtils.withCommas(System.currentTimeMillis - startMs)}ms]") ++ result._1
    msgs -> result._2
  }
}

