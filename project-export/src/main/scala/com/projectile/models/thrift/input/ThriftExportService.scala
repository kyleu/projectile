package com.projectile.models.thrift.input

import com.projectile.models.export._
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.ExportHelper.toClassName
import com.projectile.models.project.member.ServiceMember.InputType
import com.projectile.models.thrift.schema.{ThriftService, ThriftStructField}
import com.projectile.services.thrift.ThriftParseResult

object ThriftExportService {
  def loadService(s: ThriftService, metadata: ThriftParseResult.Metadata) = ExportService(
    inputType = InputType.ThriftService,
    pkg = s.pkg.toList,
    key = s.key,
    className = toClassName(s.key),
    methods = loadServiceMethods(s, metadata),
    features = Set.empty
  )

  private[this] def loadArguments(args: Seq[ThriftStructField], metadata: ThriftParseResult.Metadata) = args.zipWithIndex.map {
    case (f, idx) =>
      val (t, nativeType) = ThriftFileHelper.columnTypeFor(f.t, metadata)
      ExportField(
        key = f.key,
        propertyName = ExportHelper.toIdentifier(f.name),
        title = ExportHelper.toDefaultTitle(f.key),
        description = None,
        idx = idx,
        t = t,
        nativeType = nativeType,
        defaultValue = f.value.map(_.toString),
        notNull = f.required
      )
  }.toList

  private[this] def loadServiceMethods(s: ThriftService, metadata: ThriftParseResult.Metadata) = s.methods.map { m =>
    val args = loadArguments(m.arguments, metadata)
    val (returnType, returnNativeType) = ThriftFileHelper.columnTypeFor(m.returnType, metadata)
    ExportMethod(key = m.key, args = args, returnType = returnType, returnNativeType = returnNativeType)
  }.toList
}
