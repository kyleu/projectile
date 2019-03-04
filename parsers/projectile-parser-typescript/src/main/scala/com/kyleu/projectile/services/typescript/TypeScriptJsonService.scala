package com.kyleu.projectile.services.typescript

import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.jsdoc.JsDocHelper
import com.kyleu.projectile.models.typescript.node.TypeScriptNode._
import com.kyleu.projectile.models.typescript.node._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.{Json, JsonObject}

import scala.util.control.NonFatal

object TypeScriptJsonService {
  private[this] val commonKeys = Set("pos", "end", "flags", "kind", "jsDoc", "parent")

  def parseJson(json: Json, params: TypeScriptServiceParams): (Seq[String], TypeScriptNode) = {
    val obj = json match {
      case _ if json.isObject => json.asObject.get
      case _ => throw new IllegalStateException(s"Json [${json.noSpaces}] is not an object")
    }
    def ext[T: Decoder](k: String) = extractObj[T](obj = obj, key = k)

    val (pos, end) = ext[Int]("pos") -> ext[Int]("end")
    val kind = obj.kind()
    val jsDoc = obj("jsDoc").toSeq.flatMap(x => extract[Seq[JsonObject]](x)).map(JsDocHelper.parseJsDoc)
    val flags = NodeFlag.matching(ext[Int]("flags")).toSeq.sortBy(_.v)
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
    val ctx = NodeContext(pos = pos, end = end, kind = kind, jsDoc = jsDoc, flags = flags, keys = filteredKeys, src = src, json = finalJson)

    try {
      TypeScriptNodeService.parseNode(ctx = ctx, o = obj, params = params)
    } catch {
      case NonFatal(x) => Nil -> Error(kind = ctx.kind.toString, cls = x.getClass.getSimpleName, msg = x.toString, json = json, ctx = ctx)
    }
  }
}

