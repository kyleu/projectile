package com.projectile.models.output.feature.service

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.output.feature.ModelFeature
import com.projectile.models.output.file.ScalaFile

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
    file.addImport(config.systemPackage ++ Seq("services", "database"), "ApplicationDatabase")
    file.addImport(config.utilitiesPackage :+ "FutureUtils", "serviceContext")
    file.addImport(config.resultsPackage :+ "data", "DataField")
    file.addImport(config.applicationPackage ++ Seq("models", "auth"), "Credentials")
    file.addImport(config.resultsPackage :+ "filter", "Filter")
    file.addImport(config.resultsPackage :+ "orderBy", "OrderBy")

    file.addImport(config.utilitiesPackage :+ "tracing", "TraceData")
    file.addImport(config.utilitiesPackage :+ "tracing", "TracingService")

    if (model.pkg.nonEmpty) {
      file.addImport(config.systemPackage :+ "services", "ModelServiceHelper")
    }

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
    file.addImport(config.utilitiesPackage, "CsvUtils")
    file.add(s"def csvFor(totalCount: Int, rows: Seq[${model.className}])(implicit trace: TraceData) = {", 1)
    file.add(s"""traceB("export.csv")(td => CsvUtils.csvFor(Some(key), totalCount, rows, $queriesFilename.fields)(td))""")
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
