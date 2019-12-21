package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ._
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeContext}

object MemberHelper {
  private[this] val arrayTypes = Seq(
    "Float32Array", "Float64Array", "Uint8Array", "Uint16Array", "Uint32Array", "Int8Array", "Int16Array", "Int32Array", "Uint8ClampedArray",
    "ArrayBuffer", "ArrayBufferView", "DataView"
  )
  private[this] val jsTypes = arrayTypes.map(t => t -> s"js.typedarray.$t").toMap + ("ReadonlyArray" -> "js.Array") + ("PromiseLike" -> "js.Thenable")

  def jsType(config: ExportConfiguration, t: FieldType) = t match {
    case FieldType.StructType(key, tParams) if jsTypes.isDefinedAt(key) =>
      FieldTypeAsScala.asScala(config = config, t = FieldType.StructType(jsTypes(key), tParams), isJs = true)
    case _ => FieldTypeAsScala.asScala(config = config, t = t, isJs = true)
  }

  def addGlobal(file: ScalaFile, config: ExportConfiguration, ctx: ParseContext, name: Option[String], globalScoped: Boolean = false) = {
    file.addImport(Seq("scala", "scalajs"), "js")
    file.add("@js.native")
    if (globalScoped) {
      file.add(s"""@js.annotation.JSGlobalScope""")
    } else {
      file.add(s"""@js.annotation.JSGlobal("${(ctx.pkg ++ name.toSeq).mkString(".")}")""")
    }
  }
}

final case class MemberHelper(ctx: ParseContext, config: ExportConfiguration, file: ScalaFile) {
  def addImports(types: FieldType*) = types.foreach { t =>
    FieldTypeImports.imports(config = config, t = t, isJs = true).foreach { pkg =>
      file.addImport(p = pkg.dropRight(1), c = pkg.lastOption.getOrElse(throw new IllegalStateException()))
    }
  }

  def forType(typ: FieldTypeRequired) = {
    addImports(typ.t)
    MemberHelper.jsType(config, typ.t)
  }

  def forTParam(t: TypeParam) = t.constraint match {
    case Some(c) =>
      addImports(c)
      ExportHelper.escapeKeyword(t.name) + " <: " + FieldTypeAsScala.asScala(config = config, t = c, isJs = true)
    case None =>
      ExportHelper.escapeKeyword(t.name)
  }

  def forObj(obj: ObjectField) = {
    addImports(obj.t)
    s"${ExportHelper.escapeKeyword(obj.k)}: ${MemberHelper.jsType(config, obj.t)}"
  }

  def forMethod(name: String, tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) = {
    val paramsString = params.map(forObj).mkString(", ")
    val tParamsString = if (tParams.isEmpty) { "" } else { "[" + tParams.map(forTParam).mkString(", ") + "]" }
    val abst = ctx.modifiers(ModifierFlag.Abstract)
    val decl = if (abst) { "" } else { " = js.native" }
    file.add(s"${ov(name)}def ${ExportHelper.escapeKeyword(name)}$tParamsString($paramsString): ${forType(ret)}$decl")
    params.map(_.t) ++ tParams.flatMap(t => t.constraint.toSeq ++ t.default.toSeq)
  }

  def forDecl(name: String, typ: FieldTypeRequired, ctx: NodeContext) = {
    val decl = if (ctx.isAbstract) { "" } else { " = js.native" }
    val keyword = if (ctx.isConst) { "val" } else { "var" }
    file.add(s"$keyword ${ExportHelper.escapeKeyword(name)}: ${forType(typ)}$decl")
    Seq(typ.t)
  }

  def forIndex(typ: FieldType, params: Seq[ObjectField], ctx: NodeContext) = {
    val paramsString = params.map(forObj).mkString(", ")
    file.add("@js.annotation.JSBracketAccess")
    file.add(s"def apply($paramsString): ${FieldTypeAsScala.asScala(config = config, t = typ, isJs = true)} = js.native")
    if (!ctx.isConst) {
      file.add()
      file.add("@js.annotation.JSBracketAccess")
      file.add(s"def update($paramsString, v: ${FieldTypeAsScala.asScala(config = config, t = typ, isJs = true)}): Unit = js.native")
    }
    Seq(typ)
  }

  def forApply(tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) = {
    val paramsString = params.map(forObj).mkString(", ")
    val tParamsString = if (tParams.isEmpty) { "" } else { "[" + tParams.map(forTParam).mkString(", ") + "]" }
    val abst = ctx.modifiers(ModifierFlag.Abstract)
    val decl = if (abst) { "" } else { " = js.native" }
    file.add(s"def apply$tParamsString($paramsString): ${forType(ret)}$decl")
    params.map(_.t) ++ tParams.flatMap(t => t.constraint.toSeq ++ t.default.toSeq)
    tParams.flatMap(_.types) ++ params.map(_.t) :+ ret.t
  }

  private[this] def ov(name: String) = name match {
    case "toString" | "clone" => "override "
    case _ => ""
  }
}
