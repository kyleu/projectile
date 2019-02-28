package com.kyleu.projectile.services.typescript

import com.kyleu.projectile.models.typescript.TypeScriptNode._
import com.kyleu.projectile.models.typescript._
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

import scala.util.control.NonFatal

object TypeScriptNodeService {
  private[this] val commonKeys = Set("pos", "end", "flags", "kind", "jsDoc", "parent")

  def fromJson(json: Json) = {
    parseJson(json = json, depth = 0)
  }

  private[this] def parseNode(ctx: NodeContext, obj: JsonObject, depth: Int): TypeScriptNode = {
    def kids(k: String) = obj.ext[Seq[Json]](k).map(x => parseJson(x, depth + 1))

    ctx.kind match {
      case SyntaxKind.SourceFile => SourceFile(filename = obj.ext[String]("fileName"), statements = kids("statements"), ctx = ctx)

      // case SyntaxKind.Comment => Comment(commment, ctx = ctx)

      case SyntaxKind.InterfaceDeclaration => InterfaceDecl(name = obj.name(), members = kids("members"), ctx = ctx)
      case SyntaxKind.ModuleDeclaration => ModuleDecl(name = obj.name(), statements = kids("body.statements"), ctx = ctx)
      case SyntaxKind.ClassDeclaration => ClassDecl(name = obj.name(), members = kids("members"), ctx = ctx)
      case SyntaxKind.MethodDeclaration => MethodDecl(name = obj.name(), params = obj.params(), ret = obj.typ(), ctx = ctx)

      case SyntaxKind.Constructor => Constructor(params = obj.params(), ctx = ctx)
      case SyntaxKind.PropertySignature => PropertySig(name = obj.name(), typ = obj.typ(), ctx = ctx)
      case SyntaxKind.MethodSignature => MethodSig(name = obj.name(), ctx = ctx)

      case SyntaxKind.ExportAssignment => ExportAssignment(ctx = ctx)
      case SyntaxKind.VariableStatement => VariableStmt(declarations = kids("declarationList.declarations"), ctx = ctx)

      case _ => Unknown(kind = ctx.kind.toString, ctx = ctx)
    }
  }

  private[this] def parseJson(json: Json, depth: Int): TypeScriptNode = {
    val obj = json match {
      case _ if json.isObject => json.asObject.get
      case _ => throw new IllegalStateException(s"Json [${json.noSpaces}] is not an object")
    }
    def ext[T: Decoder](k: String) = extractObj[T](obj = obj, key = k)

    val kind = SyntaxKind.withValue(ext[Int]("kind"))
    val jsDoc = obj("jsDoc").toSeq.flatMap(x => extract[Seq[JsonObject]](x)).flatMap(_.apply("comment").map(_.as[String].right.get))
    val flags = NodeFlag.matching(ext[Int]("flags")).toSeq.sortBy(_.v)
    val filteredKeys = obj.keys.filterNot(commonKeys.apply).toList
    val ctx = NodeContext(pos = ext[Int]("pos"), end = ext[Int]("end"), kind = kind, jsDoc = jsDoc, flags = flags, keys = filteredKeys)

    try {
      parseNode(ctx = ctx, obj = obj, depth = depth)
    } catch {
      case NonFatal(x) =>
        x.printStackTrace()
        Error(kind = ctx.kind.toString, cls = x.getClass.getSimpleName, msg = x.toString, json = json, ctx = ctx)
    }
  }
}

