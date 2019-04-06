package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.node.SyntaxKind
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.JacksonUtils.printJackson
import io.circe.JsonObject

import scala.util.control.NonFatal

object TypeHelper {
  def forNode(o: JsonObject): FieldType = try {
    forNodeInternal(o, None)
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Error [${x.getMessage}] processing type [${o.kind()}] for node [${printJackson(o.asJson)}]", x)
  }

  private[this] def forNodeInternal(o: JsonObject, nameHint: Option[String]): FieldType = o.kind() match {
    case SyntaxKind.NullKeyword => ExoticType("null")
    case SyntaxKind.UndefinedKeyword => ExoticType("undefined")
    case SyntaxKind.NeverKeyword => NothingType

    case SyntaxKind.AnyKeyword => AnyType
    case SyntaxKind.VoidKeyword => UnitType

    case SyntaxKind.BooleanKeyword => BooleanType
    case SyntaxKind.TrueKeyword => BooleanType
    case SyntaxKind.FalseKeyword => BooleanType

    case SyntaxKind.NumberKeyword => DoubleType
    case SyntaxKind.NumericLiteral => try {
      extractObj[String](o, "text").toInt
      IntegerType
    } catch {
      case _: NumberFormatException => DoubleType
    }

    case SyntaxKind.StringLiteral => StringType
    case SyntaxKind.StringKeyword => StringType

    case SyntaxKind.NoSubstitutionTemplateLiteral => ExoticType("NoSubstitutionTemplateLiteral")

    case SyntaxKind.SymbolKeyword => ExoticType("SymbolKeyword")
    case SyntaxKind.ObjectKeyword => StructType(key = "js.Object", tParams = o.tParams())

    case SyntaxKind.ArrayType => ListType(o.typ("elementType"))
    case SyntaxKind.UnionType => collapseUnion(UnionType(key = o.nameOpt().getOrElse("-anon-"), types = o.kids("types").map(forNode).distinct))

    case SyntaxKind.TupleType => ExoticType("TupleType")
    case SyntaxKind.ImportType => ExoticType("Import")

    case SyntaxKind.ConstructorType => MethodType(params = o.params(), ret = ExoticType("this"))
    case SyntaxKind.FunctionType => MethodType(params = o.params(), ret = o.typ())
    case SyntaxKind.ParenthesizedType => o.typ()
    case SyntaxKind.LiteralType => o.typ("literal")
    case SyntaxKind.ConditionalType => ExoticType("ConditionalType")
    case SyntaxKind.IntersectionType => IntersectionType(key = o.nameOpt().getOrElse("-anon-"), types = o.kids("types").map(forNode).distinct)
    case SyntaxKind.IndexedAccessType => ExoticType("IndexedAccessType")
    case SyntaxKind.MappedType => ExoticType("MappedType")

    case SyntaxKind.ThisType => ThisType

    case SyntaxKind.TypeLiteral => ObjectType(key = nameHint.getOrElse("_typeliteral"), fields = o.memberFields())
    case SyntaxKind.TypeReference => MethodHelper.getName(extractObj[JsonObject](o, "typeName")) match {
      case "Array" => o.tParams("typeArguments").toList match {
        case arg :: Nil => ListType(typ = arg.constraint match {
          case Some(c) => c
          case None => StructType(arg.name)
        })
        case Nil => ListType(typ = AnyType)
        case _ => throw new IllegalStateException("Cannot handle multiple array type args")
      }
      case name => StructType(key = name, tParams = o.tParams("typeArguments"))
    }
    case SyntaxKind.TypeQuery => ExoticType("TypeQuery")
    case SyntaxKind.TypeOperator => ExoticType("TypeOperator")
    case SyntaxKind.TypePredicate => ExoticType("TypePredicate")
    case SyntaxKind.TypeParameter => StructType(o.name())
    case SyntaxKind.PrefixUnaryExpression => ExoticType("PrefixUnaryExpression")

    case SyntaxKind.UnknownKeyword => ExoticType("Unknown")

    case _ => throw new IllegalStateException(s"Cannot determine field type for kind [${o.kind()}]")
  }

  private[this] def collapseUnion(u: UnionType): FieldType = u.types.toList match {
    case h :: Nil => h
    case kids if kids.contains(IntegerType) && kids.contains(DoubleType) => collapseUnion(u.copy(types = u.types.filterNot(_ == IntegerType)))
    case _ => u
  }
}
