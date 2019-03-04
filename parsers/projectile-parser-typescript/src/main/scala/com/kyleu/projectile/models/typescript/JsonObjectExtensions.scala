package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, SyntaxKind}
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

import scala.language.implicitConversions

object JsonObjectExtensions {
  class RichJsonObject(o: JsonObject) {
    def ext[T: Decoder](k: String) = extractObj[T](obj = o, key = k)

    def typOpt(k: String = "type") = o.apply(k).map(extract[JsonObject]).map(TypeScriptTypeHelper.forNode)
    def typ(k: String = "type") = typOpt(k).getOrElse(crash(s"No type with key [$k]"))
    def typOrAny(k: String = "type") = typOpt(k).getOrElse(FieldType.AnyType)

    def nameOpt(k: String = "name") = o.apply(k).map(extract[JsonObject]).map(TypeScriptMethodHelper.getName)
    def name(k: String = "name") = nameOpt(k).getOrElse(crash(s"No name with key [$k]"))
    def nameOrDefault(k: String = "name") = nameOpt(k).getOrElse("default")

    def text() = TypeScriptMethodHelper.getName(o)
    def literal(k: String) = o.apply(k).map(extract[JsonObject]).map(TypeScriptMethodHelper.getLiteral)
    def kind() = SyntaxKind.withValue(extractObj[Int](o, "kind"))

    def kids(k: String = "children") = o.ext[Seq[JsonObject]](k)
    def params(k: String = "parameters") = ext[Seq[JsonObject]](k).map(TypeScriptMethodHelper.getParam)
    def modifiers(k: String = "modifiers") = {
      o.apply(k).map(extract[Seq[JsonObject]]).map(_.map(o => ModifierFlag.byKind(o.kind()))).getOrElse(Nil).distinct.sortBy(_.toString)
    }

    private[this] def crash(msg: String) = throw new IllegalStateException(msg + s" among candidates [${o.keys.mkString(", ")}]")
  }

  implicit def richJsonObject(o: JsonObject): RichJsonObject = new RichJsonObject(o)
}
