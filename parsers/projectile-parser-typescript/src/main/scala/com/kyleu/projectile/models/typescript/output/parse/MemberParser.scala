package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ._
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{NodeContext, NodeHelper, SyntaxKind, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.OutputHelper

object MemberParser {
  final case class Result(globalScoped: Boolean, members: Seq[TypeScriptNode], extraClasses: Seq[(String, FieldType.ObjectType, Boolean, NodeContext)])

  def filter(tsns: Seq[TypeScriptNode]): Result = {
    val members = tsns.filterNot(_.ctx.isPrivate)
    def find(n: String) = members.find {
      case node: TypeScriptNode.ModuleDecl if node.name == n => true
      case node: TypeScriptNode.VariableDecl if node.name == n => true
      case node: TypeScriptNode.MethodDecl if node.name == n => true
      case _ => false
    }

    val ambient = members.filter(m => m.ctx.kind match {
      case SyntaxKind.ExportAssignment => true
      case SyntaxKind.ExportDeclaration => true
      case SyntaxKind.ImportDeclaration => true
      case SyntaxKind.ImportEqualsDeclaration => true
      case SyntaxKind.DeclareKeyword => true
      case SyntaxKind.FunctionDeclaration => true
      case SyntaxKind.TypeAliasDeclaration => true
      case SyntaxKind.VariableDeclaration => true
      case _ => false
    })

    val (globalScoped, ret) = if (ambient.isEmpty) {
      false -> members
    } else if (ambient.size == members.size) {
      true -> ambient.flatMap {
        case e: TypeScriptNode.ExportAssignment => Some(find(e.exp).getOrElse(
          TypeScriptNode.Error(kind = e.ctx.kind.key, cls = "ExportAssignment", msg = s"Missing model for [${e.exp}]")
        ))
        case v: TypeScriptNode.VariableDecl if v.typ.t.isInstanceOf[FieldType.ObjectType] => None
        case x => Some(x)
      }
    } else {
      val missing = members.diff(ambient).map(_.ctx.kind).distinct.sortBy(_.key)
      throw new IllegalStateException(s"Only ${ambient.size} of ${members.size} members are ambient. Missing: [${missing.mkString(", ")}]")
    }
    val ext = members.collect {
      case x: TypeScriptNode.VariableDecl if x.typ.t.isInstanceOf[FieldType.ObjectType] => (x.name, x.typ.t.asInstanceOf[FieldType.ObjectType], x.typ.r, x.ctx)
    }
    Result(globalScoped = globalScoped, members = ret.distinct, extraClasses = ext)
  }

  def print(ctx: ParseContext, config: ExportConfiguration, tsn: TypeScriptNode, file: ScalaFile, last: Boolean = false): Unit = {
    val h = MemberHelper(ctx, config, file)
    OutputHelper.printContext(file, tsn.ctx)
    val typs = tsn match {
      case node: TypeScriptNode.Constructor if node.ctx.isPublic =>
        file.add(s"def this(${node.params.map(h.forObj).mkString(", ")}) = this()")
        node.params.map(_.t)
      case _: TypeScriptNode.Constructor => Nil
      case _: TypeScriptNode.ImportDecl => Nil
      case node: TypeScriptNode.VariableDecl => h.forDecl(name = node.name, typ = node.typ, ctx = node.ctx)
      case node: TypeScriptNode.PropertyDecl => h.forDecl(name = node.name, typ = node.typ, ctx = node.ctx)
      case node: TypeScriptNode.PropertySig => h.forDecl(name = node.name, typ = node.typ, ctx = node.ctx)
      case node: TypeScriptNode.MethodDecl => h.forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.MethodSig => h.forMethod(name = node.name, tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.CallSig => h.forApply(tParams = node.tParams, params = node.params, ret = node.ret, ctx = node.ctx)
      case node: TypeScriptNode.TypeAliasDecl =>
        file.add(s"type ${node.name} = ${FieldTypeAsScala.asScala(config = config, t = node.typ, isJs = true)}")
        Seq(node.typ)
      case node: TypeScriptNode.IndexSig => h.forIndex(typ = node.typ, params = node.params, ctx = node.ctx)
      case node =>
        file.add(s"// ${node.getClass.getSimpleName}:${NodeHelper.asString(node)}")
        Nil
    }

    typs.flatMap(t => FieldTypeImports.imports(config = config, t = t, isJs = true)).foreach { pkg =>
      file.addImport(pkg.dropRight(1), pkg.lastOption.getOrElse(throw new IllegalStateException()))
    }
  }
}
