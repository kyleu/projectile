package com.projectile.models.output.feature.datamodel

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.output.feature.ModelFeature
import com.projectile.models.output.file.ScalaFile

object ResultFile {
  private[this] val resultArgs = "paging = paging, filters = filters, orderBys = orderBys, totalCount = totalCount, results = results, durationMs = durationMs"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.modelPackage, key = model.className + "Result")

    file.addImport(Seq("java", "time"), "LocalDateTime")
    file.addImport(config.resultsPackage, "BaseResult")
    file.addImport(config.resultsPackage :+ "filter", "Filter")
    file.addImport(config.resultsPackage :+ "orderBy", "OrderBy")
    file.addImport(config.resultsPackage :+ "paging", "PagingOptions")

    file.addImport(config.utilitiesPackage, "DateUtils")
    file.addImport(config.utilitiesPackage :+ "JsonSerializers", "_")

    file.add(s"final case class ${model.className}Result(", 2)
    file.add("override val filters: Seq[Filter] = Nil,")
    file.add("override val orderBys: Seq[OrderBy] = Nil,")
    file.add("override val totalCount: Int = 0,")
    file.add("override val paging: PagingOptions = PagingOptions(),")
    file.add(s"override val results: Seq[${model.className}] = Nil,")
    file.add("override val durationMs: Int = 0,")
    file.add(s"override val occurred: LocalDateTime = DateUtils.now")
    file.add(s") extends BaseResult[${model.className}]", -2)

    file.add()
    file.add(s"object ${model.className}Result {", 1)
    file.add(s"implicit val jsonEncoder: Encoder[${model.className}Result] = deriveEncoder")
    file.add(s"implicit val jsonDecoder: Decoder[${model.className}Result] = deriveDecoder")
    file.add()

    file.add("def fromRecords(")
    file.add("  q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None,")
    file.add(s"  startMs: Long, totalCount: Int, results: Seq[${model.className}]")
    file.add(") = {", 1)
    file.add("val paging = PagingOptions.from(totalCount, limit, offset)")
    file.add("val durationMs = (DateUtils.nowMillis - startMs).toInt")
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
