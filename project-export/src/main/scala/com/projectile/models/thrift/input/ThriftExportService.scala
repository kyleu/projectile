package com.projectile.models.thrift.input

import com.projectile.models.export._
import com.projectile.models.output.ExportHelper.toClassName
import com.projectile.models.project.member.ServiceMember.InputType
import com.projectile.models.thrift.schema.ThriftService

object ThriftExportService {
  def loadService(s: ThriftService, enums: Seq[ExportEnum]) = ExportService(
    inputType = InputType.ThriftService,
    pkg = Nil,
    key = s.key,
    className = toClassName(s.key),
    methods = loadServiceMethods(s, enums),
    features = Set.empty
  )

  private[this] def loadServiceMethods(s: ThriftService, enums: Seq[ExportEnum]) = s.methods.zipWithIndex.map {
    case (m, _) => m.key
  }.toList
}
