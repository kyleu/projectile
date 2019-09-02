package com.kyleu.projectile.models.feature.service.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceFile {
  private[this] val inject = "@javax.inject.Inject() (val db: JdbcDatabase, override val tracing: TracingService)(implicit ec: ExecutionContext)"
  private[this] val searchArgs = "filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = model.servicePackage(config), key = model.className + "Service")
    val queriesFilename = model.className + "Queries"

    file.addImport(model.modelPackage(config), model.className)
    file.addImport(model.queriesPackage(config), model.className + "Queries")
    file.addImport(Seq("scala", "concurrent"), "Future")

    config.addCommonImport(file, "JdbcDatabase")
    config.addCommonImport(file, "ExecutionContext")
    config.addCommonImport(file, "DataField")

    config.addCommonImport(file, "Credentials")
    config.addCommonImport(file, "Filter")
    config.addCommonImport(file, "OrderBy")

    config.addCommonImport(file, "TraceData")
    config.addCommonImport(file, "TracingService")

    if (config.systemPackage.nonEmpty || model.pkg.nonEmpty) {
      config.addCommonImport(file, "ModelServiceHelper")
    }

    model.pkFields.foreach(_.addImport(config, file, Nil))

    file.add("@javax.inject.Singleton")
    val perm = if (model.features(ModelFeature.Auth)) { s""", "${model.firstPackage}" -> "${model.className}"""" } else { "" }
    file.add(s"""class ${model.className}Service $inject extends ModelServiceHelper[${model.className}]("${model.propertyName}"$perm) {""", 1)

    val viewCheck = if (model.features(ModelFeature.Auth)) { """checkPerm(creds, "view") """ } else { "" }
    ServiceHelper.addGetters(config, model, file, viewCheck)
    ServiceHelper.writeSearchFields(model, file, queriesFilename, "(implicit trace: TraceData)", searchArgs, viewCheck)
    ServiceHelper.writeForeignKeys(config, model, file, viewCheck)

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
