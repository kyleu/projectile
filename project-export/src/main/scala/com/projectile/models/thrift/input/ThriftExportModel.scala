package com.projectile.models.thrift.input

import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import com.projectile.models.project.member.ModelMember.InputType
import com.projectile.models.thrift.schema.ThriftStruct
import com.projectile.services.thrift.ThriftParseResult

object ThriftExportModel {
  def loadStructModel(s: ThriftStruct, metadata: ThriftParseResult.Metadata) = {
    val cn = toClassName(s.key)
    val title = toDefaultTitle(cn)
    val fields = loadStructFields(s, metadata)

    ExportModel(
      inputType = InputType.ThriftStruct,
      key = s.key,
      pkg = s.pkg.toList,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = title + "s",
      fields = fields
    )
  }

  private[this] def loadStructFields(s: ThriftStruct, metadata: ThriftParseResult.Metadata) = s.fields.zipWithIndex.map {
    case (f, idx) =>
      val t = ThriftFileHelper.columnTypeFor(f.t, metadata)
      ExportField(
        key = f.key,
        propertyName = ExportHelper.toIdentifier(f.name),
        title = ExportHelper.toDefaultTitle(f.key),
        description = None,
        idx = idx,
        t = t,
        nativeType = "TODO",
        defaultValue = f.value.map(_.toString),
        notNull = f.required
      )
  }.toList
}
