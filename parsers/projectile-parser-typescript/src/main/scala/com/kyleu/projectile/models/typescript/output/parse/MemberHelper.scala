package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ._
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{ModifierFlag, NodeContext}

object MemberHelper {
  private[this] val arrayTypes = Seq(
    "Float32Array", "Float64Array", "Uint8Array", "Uint16Array", "Uint32Array", "Int8Array", "Int16Array", "Int32Array", "Uint8ClampedArray",
    "ArrayBuffer", "ArrayBufferView", "DataView"
  )
  private[this] val jsTypes = arrayTypes.map(t => t -> s"js.typedarray.$t").toMap + ("ReadonlyArray" -> "js.Array") + ("PromiseLike" -> "js.Thenable")

  def jsType(config: ExportConfiguration, t: FieldType) = t match {
    case FieldType.StructType(key, tParams) if jsTypes.isDefinedAt(key) => FieldTypeAsScala.asScala(config, FieldType.StructType(jsTypes(key), tParams))
    case _ => FieldTypeAsScala.asScala(config, t)
  }
}

case class MemberHelper(ctx: ParseContext, config: ExportConfiguration, file: ScalaFile) {
  def addImport(t: FieldType) = FieldTypeImports.imports(config, t).foreach(pkg => file.addImport(pkg.init, pkg.last))

  def forType(typ: FieldTypeRequired) = {
    addImport(typ.t)
    val r = MemberHelper.jsType(config, typ.t)
    if (typ.r) { r } else { s"Option[$r]" }
  }

  def forTParam(t: TypeParam) = t.constraint match {
    case Some(c) =>
      addImport(c)
      t.name + " <: " + FieldTypeAsScala.asScala(config, c)
    case None =>
      t.name
  }

  def forObj(obj: ObjectField) = {
    addImport(obj.t)
    s"${obj.k}: ${MemberHelper.jsType(config, obj.t)}"
  }

  def forMethod(name: String, tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) = {
    val paramsString = params.map(forObj).mkString(", ")
    val tParamsString = if (tParams.isEmpty) { "" } else { "[" + tParams.map(forTParam).mkString(", ") + "]" }
    val abst = ctx.modifiers(ModifierFlag.Abstract)
    val decl = if (abst) { "" } else { " = js.native" }
    file.add(s"def $name$tParamsString($paramsString): ${forType(ret)}$decl")
  }

  def forDecl(name: String, typ: FieldTypeRequired, ctx: NodeContext) = {
    val decl = if (ctx.isAbstract) { "" } else { " = js.native" }
    val keyword = if (ctx.isConst) { "val" } else { "var" }
    file.add(s"$keyword $name: ${forType(typ)}$decl")
  }
}
