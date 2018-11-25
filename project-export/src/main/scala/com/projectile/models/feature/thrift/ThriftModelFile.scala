package com.projectile.models.feature.thrift

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.ThriftFile

object ThriftModelFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ThriftFile("models" +: model.pkg, model.className)

    file.add("namespace java " + (config.applicationPackage ++ model.modelPackage).mkString("."))
    file.add()

    val parent = ("models" +: model.pkg).map(_ => "../").mkString
    file.add(s"""include "${parent}common.thrift"""")
    file.add(s"""include "${parent}result.thrift"""")
    file.add()

    file.add(s"struct ${model.className} {", 1)
    model.fields.foreach { field =>
      val thriftType = ExportFieldThrift.thriftType(field.t, field.nativeType, field.enumOpt(config))
      val thriftVisibility = if (field.notNull) { "required" } else { "optional" }
      file.add(s"${field.idx + 1}: $thriftVisibility $thriftType ${field.propertyName};")
    }
    file.add("}", -1)
    file.add()
    file.add(s"struct ${model.className}Result {", 1)
    file.add("1: required list<result.Filter> filters;")
    file.add("2: required list<result.OrderBy> orderBys;")
    file.add("3: required common.int totalCount;")
    file.add("4: required result.PagingOptions paging;")
    file.add(s"5: required list<${model.className}> results;")
    file.add("6: required common.int durationMs;")
    file.add("7: required common.LocalDateTime occurred;")
    file.add("}", -1)

    file
  }
}
