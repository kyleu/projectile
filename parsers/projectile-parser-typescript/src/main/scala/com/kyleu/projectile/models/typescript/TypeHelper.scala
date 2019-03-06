package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.export.typ.FieldType.ExoticType
import com.kyleu.projectile.models.export.typ.{FieldType, ObjectField}
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.node.SyntaxKind
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.JacksonUtils.printJackson
import io.circe.JsonObject

import scala.util.control.NonFatal

object TypeHelper {
  def forNode(o: JsonObject): FieldType = forNodeInternal(o)

  private[this] def forNodeInternal(o: JsonObject): FieldType = try {
    o.kind() match {
      case SyntaxKind.NullKeyword => FieldType.ExoticType("null")
      case SyntaxKind.UndefinedKeyword => FieldType.ExoticType("undefined")
      case SyntaxKind.NeverKeyword => FieldType.ExoticType("never")

      case SyntaxKind.AnyKeyword => FieldType.AnyType
      case SyntaxKind.VoidKeyword => FieldType.UnitType
      case SyntaxKind.BooleanKeyword => FieldType.BooleanType
      case SyntaxKind.NumberKeyword => FieldType.DoubleType
      case SyntaxKind.StringKeyword => FieldType.StringType
      case SyntaxKind.SymbolKeyword => FieldType.ExoticType("SymbolKeyword")
      case SyntaxKind.ObjectKeyword => FieldType.ExoticType("ObjectKeyword")

      case SyntaxKind.ArrayType => FieldType.ListType(FieldType.StringType)
      case SyntaxKind.UnionType => FieldType.UnionType(key = o.nameOpt().getOrElse("-anon-"), types = o.kids("types").map(forNode))
      case SyntaxKind.TupleType => FieldType.ExoticType("TupleType")
      case SyntaxKind.ImportType => FieldType.ExoticType("Import")

      case SyntaxKind.ConstructorType => FieldType.MethodType(params = o.params(), ret = ExoticType("this"))
      case SyntaxKind.FunctionType => FieldType.MethodType(params = o.params(), ret = o.typ())
      case SyntaxKind.ParenthesizedType => FieldType.ExoticType("ParenthesizedType")
      case SyntaxKind.LiteralType => FieldType.ExoticType("LiteralType")
      case SyntaxKind.ConditionalType => FieldType.ExoticType("ConditionalType")
      case SyntaxKind.IntersectionType => FieldType.ExoticType("IntersectionType")
      case SyntaxKind.IndexedAccessType => FieldType.ExoticType("IndexedAccessType")
      case SyntaxKind.MappedType => FieldType.ExoticType("MappedType")

      case SyntaxKind.ThisType => FieldType.ExoticType("this")

      case SyntaxKind.TypeLiteral => FieldType.ObjectType(key = "-literal-", fields = Seq(ObjectField("TODO", FieldType.StringType)))
      case SyntaxKind.TypeReference => FieldType.StructType(key = MethodHelper.getName(extractObj[JsonObject](o, "typeName")))
      case SyntaxKind.TypeQuery => FieldType.ExoticType("TypeQuery")
      case SyntaxKind.TypeOperator => FieldType.ExoticType("TypeOperator")
      case SyntaxKind.TypePredicate => FieldType.ExoticType("TypePredicate")

      case SyntaxKind.UnknownKeyword => FieldType.ExoticType("Unknown")

      case _ => throw new IllegalStateException(s"Cannot determine field type for kind [${o.kind()}]")
    }
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Error [${x.getMessage}] processing type [${o.kind()}] for node [${printJackson(o.asJson)}]", x)
  }
}
