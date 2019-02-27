package com.kyleu.projectile.services.typescript

import com.kyleu.projectile.models.typescript.TypeScriptNode._
import com.kyleu.projectile.models.typescript.{NodeContext, SyntaxKind, TypeScriptNode}
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

import scala.util.control.NonFatal

object TypeScriptNodeService {
  private[this] val commonKeys = Set("pos", "end", "flags", "kind", "jsDoc", "parent")

  def fromJson(json: Json) = {
    parseJson(json = json, depth = 0)
  }

  private[this] def parseNode(ctx: NodeContext, obj: JsonObject, depth: Int): TypeScriptNode = {
    def ext[T: Decoder](k: String) = extractObj[T](obj = obj, key = k)
    def kids(k: String) = ext[Seq[Json]](k).map(x => parseJson(x, depth + 1))
    def name() = {
      val n = ext[JsonObject]("name")
      n.apply("escapedText").orElse(n.apply("text")).map(_.as[String].right.get).getOrElse(throw new IllegalStateException(s"No name in json [$n]"))
    }

    ctx.kind match {
      case SyntaxKind.SourceFile => SourceFile(filename = ext[String]("fileName"), statements = kids("statements"), ctx = ctx)
      case SyntaxKind.InterfaceDeclaration => InterfaceDecl(name = name(), members = kids("members"), ctx = ctx)
      case SyntaxKind.ModuleDeclaration => ModuleDecl(name = name(), statements = kids("body.statements"), ctx = ctx)
      case SyntaxKind.ClassDeclaration => ClassDecl(name = name(), ctx = ctx)
      case SyntaxKind.PropertySignature => PropertySig(name = name(), ctx = ctx)
      case SyntaxKind.MethodSignature => MethodSig(name = name(), ctx = ctx)
      case SyntaxKind.VariableStatement => VariableStatement(declarations = kids("declarationList.declarations"), ctx = ctx)
      case _ => Unknown(kind = ctx.kind.toString, ctx = ctx)
    }
  }

  private[this] def parseJson(json: Json, depth: Int): TypeScriptNode = {
    val obj = json match {
      case _ if json.isObject => json.asObject.get
      case _ => throw new IllegalStateException(s"Json [${json.noSpaces}] is not an object")
    }
    def ext[T: Decoder](k: String) = extractObj[T](obj = obj, key = k)

    val jsDoc = obj("jsDoc").toSeq.flatMap(x => extract[Seq[JsonObject]](x)).flatMap(_.apply("comment").map(_.as[String].right.get))
    val filteredKeys = obj.keys.filterNot(commonKeys.apply).toList
    val ctx = NodeContext(pos = ext[Int]("pos"), end = ext[Int]("end"), kind = SyntaxKind.withValue(ext[Int]("kind")), jsDoc = jsDoc, keys = filteredKeys)

    try {
      parseNode(ctx = ctx, obj = obj, depth = depth)
    } catch {
      case NonFatal(x) =>
        val jsonStr = json.spaces2 match {
          case s if s.length > 1000 => s.take(1000).mkString + "..."
          case s => s
        }
        Error(kind = ctx.kind.toString, cls = x.getClass.getSimpleName, msg = x.toString, json = jsonStr, ctx = ctx)
    }
  }
}

