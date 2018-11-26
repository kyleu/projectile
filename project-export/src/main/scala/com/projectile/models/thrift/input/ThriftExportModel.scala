package com.projectile.models.thrift.input

import com.projectile.models.export.FieldType
import com.projectile.models.export.{ExportEnum, ExportField, ExportModel}
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.projectile.models.project.member.ModelMember.InputType
import com.projectile.models.thrift.schema.ThriftStruct

object ThriftExportModel {
  def loadStructModel(s: ThriftStruct, structs: Seq[ThriftStruct], enums: Seq[ExportEnum]) = {
    val cn = toClassName(s.key)
    val title = toDefaultTitle(cn)

    ExportModel(
      inputType = InputType.ThriftStruct,
      key = s.key,
      pkg = Nil,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = title + "s",
      fields = loadStructFields(s, enums)
    )
  }

  private[this] def loadStructFields(s: ThriftStruct, enums: Seq[ExportEnum]) = s.fields.zipWithIndex.map {
    case (f, idx) => ExportField(
      key = f.key,
      propertyName = ExportHelper.toIdentifier(f.key),
      title = ExportHelper.toDefaultTitle(f.key),
      description = None,
      idx = idx,
      t = FieldType.ComplexType,
      nativeType = f.t.toString, // TODO!!!
      defaultValue = f.value.map(_.toString),
      notNull = f.required
    )
  }.toList
}
