package com.projectile.models.thrift.input

import com.projectile.models.export._
import com.projectile.models.input.ServiceInputType
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.ExportHelper.toClassName
import com.projectile.models.thrift.schema.{ThriftService, ThriftStructField}

object ThriftExportService {
  def loadService(s: ThriftService, input: ThriftInput) = ExportService(
    inputType = ServiceInputType.ThriftService,
    pkg = s.pkg.toList :+ "services",
    key = s.key,
    className = toClassName(s.key),
    methods = loadServiceMethods(s, input),
    features = Set.empty
  )

  private[this] def loadArguments(args: Seq[ThriftStructField], input: ThriftInput) = args.zipWithIndex.map {
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

  private[this] def loadServiceMethods(s: ThriftService, input: ThriftInput) = s.methods.map { m =>
    val args = loadArguments(m.arguments, input)
    val returnType = ThriftFileHelper.columnTypeFor(m.returnType, input)
    ExportMethod(key = m.key, args = args, returnType = returnType)
  }.toList
}
