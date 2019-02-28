package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.export.typ.{FieldType, ObjectField}
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object TypeScriptTypeHelper {
  def forNode(o: JsonObject): FieldType = {
    val kind = SyntaxKind.withValue(extractObj[Int](o, "kind"))
    kind match {
      case SyntaxKind.VoidKeyword => FieldType.UnitType
      case SyntaxKind.BooleanKeyword => FieldType.BooleanType
      case SyntaxKind.NumberKeyword => FieldType.DoubleType
      case SyntaxKind.StringKeyword => FieldType.StringType
      case SyntaxKind.TypeReference => FieldType.StructType(key = getName(extractObj[JsonObject](o, "typeName")))
      case SyntaxKind.FunctionType => FieldType.MethodType(params = o.params(), ret = o.typ())
      case SyntaxKind.TypeLiteral => FieldType.ObjectType(key = "-literal-", fields = Seq(ObjectField("TODO", FieldType.StringType)))
      case _ => throw new IllegalStateException(s"Cannot determine Scala type for kind [$kind]")
    }
  }

  def getName(o: JsonObject) = o.apply("escapedText").orElse(o.apply("text")).map(_.as[String].right.get).getOrElse {
    throw new IllegalStateException(s"No name in json with keys [${o.keys.mkString(", ")}]")
  }

  def getParam(o: JsonObject) = {
    ObjectField(k = getName(extractObj[JsonObject](o, "name")), t = forNode(extractObj[JsonObject](o, "type")))
  }
}
