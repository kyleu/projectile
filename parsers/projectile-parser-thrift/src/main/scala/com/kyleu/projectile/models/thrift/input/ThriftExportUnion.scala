package com.kyleu.projectile.models.thrift.input

import com.kyleu.projectile.models.export.ExportUnion
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper.toClassName
import com.kyleu.projectile.models.thrift.schema.ThriftUnion

object ThriftExportUnion {
  def forThriftName(s: String) = s

  def loadUnion(u: ThriftUnion, input: ThriftInput) = ExportUnion(
    inputType = InputType.Union.ThriftUnion,
    pkg = u.pkg.toList :+ "unions",
    key = u.key,
    className = forThriftName(toClassName(u.key)),
    types = u.types.map(t => t.asField(input)).toList
  )
}
