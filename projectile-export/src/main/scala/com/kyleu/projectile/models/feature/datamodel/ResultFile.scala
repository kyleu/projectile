package com.kyleu.projectile.models.feature.datamodel

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ResultFile {
  private[this] val resultArgs = "paging = paging, filters = filters, orderBys = orderBys, totalCount = totalCount, results = results, durationMs = durationMs"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = model.modelPackage(config), key = model.className + "Result")

    file.addImport(Seq("java", "time"), "LocalDateTime")
    config.addCommonImport(file, "BaseResult")
    config.addCommonImport(file, "Filter")
    config.addCommonImport(file, "OrderBy")
    config.addCommonImport(file, "PagingOptions")

    config.addCommonImport(file, "DateUtils")

    config.addCommonImport(file, "JsonSerializers", "_")

    file.add(s"final case class ${model.className}Result(", 2)
    file.add("override val filters: Seq[Filter] = Nil,")
    file.add("override val orderBys: Seq[OrderBy] = Nil,")
    file.add("override val totalCount: Int = 0,")
    file.add("override val paging: PagingOptions = PagingOptions(),")
    file.add(s"override val results: Seq[${model.className}] = Nil,")
    file.add("override val durationMs: Int = 0,")
    file.add("override val occurred: LocalDateTime = DateUtils.now")
    file.add(s") extends BaseResult[${model.className}]", -2)

    file.add()
    file.add(s"object ${model.className}Result {", 1)
    addJson(config, file, model)

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

  private[this] def addJson(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = if (model.features(ModelFeature.Json)) {
    // file.add(s"implicit val jsonEncoder: Encoder[${model.className}Result] = deriveEncoder")
    // file.add(s"implicit val jsonDecoder: Decoder[${model.className}Result] = deriveDecoder")

    val fields = Seq(
      "filters" -> "Seq[Filter]", "orderBys" -> "Seq[OrderBy]", "totalCount" -> "Int", "paging" -> "PagingOptions",
      "results" -> s"Seq[${model.className}]", "durationMs" -> "Int", "occurred" -> "LocalDateTime"
    )

    file.add(s"implicit val jsonEncoder: Encoder[${model.className}Result] = (r: ${model.className}Result) => io.circe.Json.obj(", 1)
    fields.foreach { f =>
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(s"""("${f._1}", r.${f._1}.asJson)$comma""")
    }
    file.add(")", -1)
    file.add()

    file.add(s"implicit val jsonDecoder: Decoder[${model.className}Result] = (c: io.circe.HCursor) => for {", 1)
    fields.foreach { f =>
      file.add(s"""${f._1} <- c.downField("${f._1}").as[${f._2}]""")
    }
    val props = fields.map(_._1).mkString(", ")
    file.add(s"} yield ${model.className}Result($props)", -1)
    file.add()
  }

}
