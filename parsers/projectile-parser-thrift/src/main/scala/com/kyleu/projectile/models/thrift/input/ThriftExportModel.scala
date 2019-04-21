package com.kyleu.projectile.models.thrift.input

import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.kyleu.projectile.models.thrift.schema.ThriftStruct

object ThriftExportModel {
  def forThriftName(s: String) = s

  def loadStructModel(s: ThriftStruct, input: ThriftInput) = {
    val cn = forThriftName(toClassName(s.key))
    val title = toDefaultTitle(cn)
    val fields = loadStructFields(s, input)

    ExportModel(
      inputType = InputType.Model.ThriftStruct,
      key = s.key,
      pkg = s.pkg.toList :+ "models",
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = ExportHelper.toDefaultPlural(title),
      arguments = Nil,
      fields = fields
    )
  }

  private[this] def loadStructFields(s: ThriftStruct, input: ThriftInput) = s.fields.map { f =>
    val t = ThriftFileHelper.columnTypeFor(f.t, input)
    ExportField(
      key = f.key,
      propertyName = f.name,
      title = ExportHelper.toDefaultTitle(f.key),
      description = None,
      t = t,
      defaultValue = f.value.map(_.toString),
      required = f.required || f.value.isDefined
    )
  }.toList
}
