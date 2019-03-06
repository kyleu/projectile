package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.{Error, SourceFile, Unknown}
import com.kyleu.projectile.services.typescript.FileService.parseFile
import com.kyleu.projectile.services.typescript.ServiceParams
import io.circe.{Json, JsonObject}

object SourceFileHelper {
  private[this] val commentTokens = Set("//", "/*", "*/") // TODO Support multiline comments for real
  private[this] val authorKey = "Definitions by: "

  def parseSourceFile(ctx: NodeContext, obj: JsonObject, getKids: => Seq[TypeScriptNode], params: ServiceParams): (Seq[String], TypeScriptNode) = {
    val header = parseHeader(src = ctx.src)
    val includePaths = if (params.parseRefs) {
      header.refs.filter(_.startsWith("path:")).map(_.stripPrefix("path:").trim.dropWhile(c => c == '/' || c == '.'))
    } else {
      Nil
    }
    val dir = if (params.file.isDirectory) { params.file } else { params.file.parent }
    val includes = includePaths.map(p => p -> (dir / p match {
      case x if x.isDirectory => x / "index.d.ts"
      case x if x.name.endsWith("ts") => x
      case x if (x.parent / (x.name + ".d.ts")).exists => x.parent / (x.name + ".d.ts")
      case x => x.parent / (x.name + ".ts")
    })).map {
      case f if !params.parseRefs => Nil -> Unknown(s"reference:${f._1}", Json.fromString(params.root.relativize(f._2).toString), ctx)
      case f if f._2.exists && f._2.isReadable =>
        val p = params.root.relativize(f._2).toString
        if (params.encountered(p)) {
          throw new IllegalStateException("!!!")
        } else {
          parseFile(root = params.root, cache = params.cache, path = p, encountered = params.encountered + p)
        }
      case f => Nil -> Error(kind = "reference", cls = f._1, msg = "File not found", json = Json.fromString(f._2.pathAsString), ctx = ctx)
    }
    val msgs = includes.flatMap(_._1)
    msgs -> SourceFile(path = obj.ext[String]("fileName"), header = header, statements = includes.map(_._2) ++ getKids, ctx = ctx)
  }

  private[this] def parseHeader(src: Seq[String]) = {
    val (referenceLines, headerLines) = src.takeWhile(line => commentTokens.exists(line.contains) || line.trim.isEmpty).partition(_.contains("<reference"))
    val headerContent = headerLines.filterNot(_.contains(" tslint:")).dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse

    val references = referenceLines.collect {
      case x if x.contains(" types") =>
        val qIdx = quoteIndex(x, x.indexOf(" types"))
        removeQuotes(x.substring(qIdx, quoteIndex(x, qIdx + 1)))
      case x if x.contains(" path") =>
        val qIdx = quoteIndex(x, x.indexOf(" path"))
        "path:" + removeQuotes(x.substring(qIdx, quoteIndex(x, qIdx + 1)))
      case x => x
    }
    val projectName = findLine("Type definitions for ", headerContent)
    val projectUrl = findLine("Project: ", headerContent).map(_.stripSuffix("/"))
    val definitionsUrl = findLine("Definitions: ", headerContent)
    val authors = findLine(authorKey, headerContent).toSeq.flatMap { _ =>
      headerContent.dropWhile(!_.contains(authorKey)).reverse.dropWhile(!_.contains("          ")).reverse.map { l =>
        l.trim.stripPrefix("//").trim.stripPrefix(authorKey).trim
      }
    }
    val header = SourceFileHeader(
      projectName = projectName,
      projectUrl = projectUrl,
      authors = authors,
      definitionsUrl = definitionsUrl,
      refs = references,
      content = headerContent
    )
    header
  }

  private[this] def findLine(k: String, lines: Seq[String]) = lines.find(_.contains(k)).map { line =>
    line.substring(line.indexOf(k) + k.length).trim
  }
  private[this] def removeQuotes(s: String) = s.trim.dropWhile(c => c == '\"' || c == '\'').reverse.dropWhile(c => c == '\"' || c == '\'').reverse.trim
  private[this] def quoteIndex(s: String, startIdx: Int = 0) = s.indexOf('\'', startIdx) match {
    case -1 => s.indexOf('\"', startIdx) match {
      case -1 => throw new IllegalStateException(s"Cannot find quote after index [$startIdx] for string [$s]")
      case idx => idx
    }
    case idx => idx
  }
}
