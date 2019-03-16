package com.kyleu.projectile.services.typescript

import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.jsdoc.{JsDocHelper, JsDocNode}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode._
import com.kyleu.projectile.models.typescript.node._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.{Json, JsonObject}

import scala.util.control.NonFatal

object JsonService {
  private[this] val commonKeys = Set("pos", "end", "flags", "kind", "jsDoc", "parent")

  def nodeContext(json: Json, params: ServiceParams) = {
    val obj = json match {
      case _ if json.isObject => json.asObject.get
      case _ => throw new IllegalStateException(s"Json [${json.noSpaces}] is not an object")
    }
    def ext[T: Decoder](k: String) = extractObj[T](obj = obj, key = k)
    val kind = obj.kind()

    val (pos, end) = ext[Int]("pos") -> ext[Int]("end")

    val jsDoc = obj("jsDoc").toSeq.foldLeft(JsDocNode.empty) { (l, r) =>
      extract[Seq[JsonObject]](r).map(JsDocHelper.parseJsDoc).foldLeft(l)((x, y) => x.merge(y))
    }.emptyOpt
    val flags = NodeFlag.matching(ext[Int]("flags")).toSeq.sortBy(_.v)
    val mods = obj.modifiers().toSet
    val filteredKeys = obj.keys.filterNot(commonKeys.apply).toList
    var src = if (pos < 0 || pos > params.sourcecode.length || end < 0 || end > params.sourcecode.length) {
      Seq(s"Index [$pos:$end] out of range for file size [${params.sourcecode.length}]")
    } else {
      params.sourcecode.substring(pos, end).split('\n').toList
    }
    var indented = src.nonEmpty && src.forall(_.headOption.forall(_ == ' '))
    while (indented) {
      src = src.map(_.stripPrefix(" "))
      indented = src.forall(_.headOption.forall(_ == ' '))
    }
    src = src.dropWhile(_.isEmpty).reverse.dropWhile(_.isEmpty).reverse
    val finalJson = if (NodeContext.shouldIncludeJson(kind)) { json } else { Json.Null }
    NodeContext(pos = pos, end = end, kind = kind, jsDoc = jsDoc, flags = flags, modifiers = mods, keys = filteredKeys, src = src, json = finalJson)
  }

  def parseJson(json: Json, params: ServiceParams): (Seq[String], TypeScriptNode) = {
    val obj = json match {
      case _ if json.isObject => json.asObject.get
      case _ => throw new IllegalStateException(s"Json [${json.noSpaces}] is not an object")
    }
    def ext[T: Decoder](k: String) = extractObj[T](obj = obj, key = k)
    val ctx = nodeContext(json, params)
    try {
      NodeService.parseNode(ctx = ctx, o = obj, params = params)
    } catch {
      case NonFatal(x) =>
        x.printStackTrace()
        Nil -> Error(kind = ctx.kind.toString, cls = x.getClass.getSimpleName, msg = x.toString, json = json, ctx = ctx)
    }
  }
}

