package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.export.typ.{FieldType, ObjectField, TypeParam}
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.node.SyntaxKind._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

import scala.util.control.NonFatal

object MethodHelper {
  def getLiteral(o: JsonObject): Json = {
    val textOpt = o.apply("escapedText").orElse(o.apply("text")).map(extract[String])
    def text = textOpt.getOrElse(throw new IllegalStateException(s"No [text] available among candidates [${o.keys.mkString(", ")}]"))
    def objMember(k: String) = extractObj[JsonObject](o, k)

    o.apply("kind").map(extract[Int]).map(withValue).getOrElse(StringLiteral) match {
      case NumericLiteral => text.trim.toLong.asJson
      case BigIntLiteral => BigInt(text.trim).asJson
      case StringLiteral => text.trim.asJson
      case PrefixUnaryExpression =>
        (TokenHelper.getToken(withValue(o.ext[Int]("operator"))) + extractObj[String](objMember("operand"), "text")).asJson
      case BinaryExpression =>
        val l = o.apply("left").map(asObj).map(getLiteral)
        val op = TokenHelper.getToken(extractObj[JsonObject](o, "operatorToken").kind())
        val r = o.apply("right").map(asObj).map(getLiteral)
        JsonObject("left" -> l.asJson, "op" -> op.asJson, "right" -> r.asJson).asJson
      case ParenthesizedExpression => "(todo)".asJson
      case PropertyAccessExpression => o.literal("initializer").getOrElse("-empty-".asJson)
      case x => throw new IllegalStateException(s"Unhandled literal [$x]")
    }
  }

  def getName(o: JsonObject): String = o.apply("escapedText").orElse(o.apply("text")).map(_.as[String].right.get).getOrElse {
    o.apply("right") match {
      case Some(r) => o.apply("left").map(l => getName(asObj(l))).map(_ + ".").getOrElse("") + getName(asObj(r))
      case None => o.apply("expression") match {
        case Some(r) =>
          val exp = o.apply("expression").flatMap(l => asObj(l).apply("expression").map(asObj)).map(getName)
          exp.map(_ + ".").getOrElse("") + asObj(r).apply("name").map(asObj).map(getName).getOrElse("unnamed")
        case None => o.apply("elements") match {
          case Some(_) => "-expression-"
          case None => throw new IllegalStateException(s"No name in json with keys [${o.keys.mkString(", ")}]")
        }
      }
    }
  }

  def getParam(o: JsonObject) = {
    val k = try { getName(extractObj[JsonObject](o, "name")) } catch { case NonFatal(x) => "_default" }
    val t = o.apply("type").map(extract[JsonObject]).map(TypeHelper.forNode).getOrElse(FieldType.AnyType)
    ObjectField(k = k, t = t, req = o.apply("questionToken").isEmpty, readonly = true)
  }

  def getTParam(o: JsonObject) = {
    val name = if (o.contains("name")) {
      getName(extractObj[JsonObject](o, "name"))
    } else if (o.contains("typeName")) {
      getName(extractObj[JsonObject](o, "typeName"))
    } else {
      "_"
    }
    val constraint = name match {
      case "_" => Some(TypeHelper.forNode(o))
      case _ => o.apply("constraint").map(extract[JsonObject]).map(TypeHelper.forNode)
    }
    TypeParam(
      name = name,
      constraint = constraint,
      default = o.apply("default").map(extract[JsonObject]).map(TypeHelper.forNode)
    )
  }

  private[this] def asObj(j: Json) = j.as[JsonObject] match {
    case Right(o) => o
    case Left(x) => throw x
  }
}
