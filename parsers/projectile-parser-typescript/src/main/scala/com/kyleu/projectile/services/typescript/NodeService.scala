package com.kyleu.projectile.services.typescript

import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.models.typescript.node.TypeScriptNode._
import com.kyleu.projectile.models.typescript.node.{NodeContext, SourceFileHelper, SyntaxKind, TypeScriptNode}
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object NodeService {
  def parseNode(ctx: NodeContext, o: JsonObject, params: ServiceParams): (Seq[String], TypeScriptNode) = {
    var messages = params.messages
    def addMessages(x: (Seq[String], TypeScriptNode)) = { messages = messages ++ x._1; x._2 }
    def kids(k: String = "members") = {
      o.kids(k).map(x => JsonService.parseJson(x.asJson, params.copy(depth = params.depth + 1))).map(addMessages)
    }
    def body(ob: JsonObject = o) = ob.apply("statements").map(_ => kids("statements")).getOrElse(ob.apply("body") match {
      case Some(body) => Seq(JsonService.parseJson(body, params.copy(depth = params.depth + 1))._2)
      case None => throw new IllegalStateException(s"Cannot extract statements or body from [${ob.keys.mkString(", ")}]")
    })

    val node = ctx.kind match {
      case SyntaxKind.SourceFile => addMessages(SourceFileHelper.parseSourceFile(ctx = ctx, obj = o, getKids = kids("statements"), params = params))

      case SyntaxKind.ImportDeclaration => ImportDecl(ctx = ctx)
      case SyntaxKind.ImportEqualsDeclaration => ImportDecl(ctx = ctx)
      case SyntaxKind.ExportDeclaration => ExportDecl(ctx = ctx)
      case SyntaxKind.NamespaceExportDeclaration => ExportNamespaceDecl(name = o.name(), ctx = ctx)
      case SyntaxKind.InterfaceDeclaration => InterfaceDecl(name = o.name(), members = kids(), ctx = ctx)
      case SyntaxKind.ModuleDeclaration => ModuleDecl(name = o.name(), statements = body(), ctx = ctx)
      case SyntaxKind.ClassDeclaration => ClassDecl(name = o.name(), members = kids(), ctx = ctx)
      case SyntaxKind.MethodDeclaration => MethodDecl(name = o.name(), tParams = o.tParams(), params = o.params(), ret = o.typRet(), ctx = ctx)
      case SyntaxKind.FunctionDeclaration => MethodDecl(name = o.nameOrDefault(), tParams = o.tParams(), params = o.params(), ret = o.typRet(), ctx = ctx)
      case SyntaxKind.VariableDeclaration => VariableDecl(name = o.name(), typ = o.typRet(), ctx = ctx)
      case SyntaxKind.TypeAliasDeclaration => TypeAliasDecl(name = o.name(), typ = o.typ(), ctx = ctx)
      case SyntaxKind.PropertyDeclaration => PropertyDecl(name = o.name(), typ = o.typRet(), ctx = ctx)

      case SyntaxKind.EnumDeclaration => EnumDecl(name = o.name(), members = kids(), ctx = ctx)
      case SyntaxKind.EnumMember => EnumMember(name = o.name(), initial = o.literal("initializer"), ctx = ctx)

      case SyntaxKind.ModuleBlock => ModuleBlock(statements = body(), ctx = ctx)

      case SyntaxKind.Constructor => Constructor(params = o.params(), ctx = ctx)
      case SyntaxKind.ConstructSignature => ConstructSig(typ = o.typ(), params = o.params(), ctx = ctx)
      case SyntaxKind.IndexSignature => IndexSig(typ = o.typ(), params = o.params(), ctx = ctx)
      case SyntaxKind.PropertySignature => PropertySig(name = o.name(), typ = o.typRet(), ctx = ctx)
      case SyntaxKind.CallSignature => CallSig(params = o.params(), ret = o.typRet(), ctx = ctx)
      case SyntaxKind.MethodSignature => MethodSig(name = o.name(), tParams = o.tParams(), params = o.params(), ret = o.typRet(), ctx = ctx)

      case SyntaxKind.ExportAssignment => ExportAssignment(exp = o.name("expression"), ctx = ctx)
      case SyntaxKind.VariableStatement => VariableStmt(declarations = kids("declarationList.declarations"), ctx = ctx)

      case _ => Unknown(kind = ctx.kind.toString, json = o.asJson, ctx = ctx)
    }
    messages -> node
  }
}

