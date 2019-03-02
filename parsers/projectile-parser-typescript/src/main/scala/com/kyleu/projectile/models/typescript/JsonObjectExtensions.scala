package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

import scala.language.implicitConversions
import scala.util.control.NonFatal

object JsonObjectExtensions {
  class RichJsonObject(o: JsonObject) {
    def ext[T: Decoder](k: String) = extractObj[T](obj = o, key = k)
    def typ(k: String = "type") = TypeScriptTypeHelper.forNode(ext[JsonObject](k))
    def params(k: String = "parameters") = ext[Seq[JsonObject]](k).map(TypeScriptTypeHelper.getParam)
    def name() = TypeScriptTypeHelper.getName(extractObj[JsonObject](obj = o, key = "name"))
    def nameOpt() = try { Some(name()) } catch { case NonFatal(x) => None }
  }

  implicit def richJsonObject(o: JsonObject): RichJsonObject = new RichJsonObject(o)
}
