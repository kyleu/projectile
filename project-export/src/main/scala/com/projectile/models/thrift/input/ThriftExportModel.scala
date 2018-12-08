package com.projectile.models.thrift.input

import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.projectile.models.project.member.ModelMember.InputType
import com.projectile.models.thrift.schema.ThriftStruct

object ThriftExportModel {
  def loadStructModel(s: ThriftStruct, input: ThriftInput) = {
    val cn = toClassName(s.key)
    val title = toDefaultTitle(cn)
    val fields = loadStructFields(s, input)

    ExportModel(
      inputType = InputType.ThriftStruct,
      key = s.key,
      pkg = s.pkg.toList :+ "models",
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = title + "s",
      fields = fields
    )
  }

  private[this] def loadStructFields(s: ThriftStruct, input: ThriftInput) = s.fields.zipWithIndex.map {
    case (f, idx) =>
      val t = ThriftFileHelper.columnTypeFor(f.t, input)
      ExportField(
        key = f.key,
        propertyName = ExportHelper.toIdentifier(f.name),
        title = ExportHelper.toDefaultTitle(f.key),
        description = None,
        idx = idx,
        t = t,
        defaultValue = f.value.map(_.toString),
        required = f.required
      )
  }.toList
}
