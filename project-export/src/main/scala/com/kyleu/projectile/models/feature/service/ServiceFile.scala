package com.kyleu.projectile.models.feature.service

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldTypeRestrictions
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceFile {
  private[this] val inject = "@javax.inject.Inject() (override val tracing: TracingService)"
  private[this] val searchArgs = "filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.servicePackage, key = model.className + "Service")
    val queriesFilename = model.className + "Queries"

    file.addImport(config.applicationPackage ++ model.modelPackage, model.className)
    file.addImport(config.applicationPackage ++ model.queriesPackage, model.className + "Queries")
    file.addImport(Seq("scala", "concurrent"), "Future")

    config.addCommonImport(file, "ApplicationDatabase")
    config.addCommonImport(file, "FutureUtils", "serviceContext")
    config.addCommonImport(file, "DataField")

    config.addCommonImport(file, "Credentials")
    config.addCommonImport(file, "Filter")
    config.addCommonImport(file, "OrderBy")

    config.addCommonImport(file, "TraceData")
    config.addCommonImport(file, "TracingService")

    if (model.pkg.nonEmpty) {
      config.addCommonImport(file, "ModelServiceHelper")
    }

    model.pkFields.foreach(_.addImport(config, file, Nil))

    file.add("@javax.inject.Singleton")
    file.add(s"""class ${model.className}Service $inject extends ModelServiceHelper[${model.className}]("${model.propertyName}") {""", 1)
    ServiceHelper.addGetters(config, model, file)
    if (model.propertyName != "audit") {
      file.addMarker("string-search", InjectSearchParams(model).toString)
    }

    ServiceHelper.writeSearchFields(model, file, queriesFilename, "(implicit trace: TraceData)", searchArgs)
    ServiceHelper.writeForeignKeys(config, model, file)

    if (!model.readOnly) {
      ServiceInserts.insertsFor(config, model, queriesFilename, file)
      ServiceMutations.mutations(config, model, file)
    }

    file.add()
    config.addCommonImport(file, "CsvUtils")
    file.add(s"def csvFor(totalCount: Int, rows: Seq[${model.className}])(implicit trace: TraceData) = {", 1)
    file.add(s"""traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, $queriesFilename.fields)(td))""")
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
