package com.kyleu.projectile.models.feature.service.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceTestFiles {
  def export(config: ExportConfiguration) = {
    val models = config.models.filter(m => m.inputType == InputType.Model.PostgresTable && m.features(ModelFeature.Service))
    if (models.isEmpty) { Nil } else { Seq(exportTestServices(config, models), exportTestModelSupport(config, models)) }
  }

  private[this] def exportTestServices(config: ExportConfiguration, models: Seq[ExportModel]) = {
    val file = ScalaFile(path = OutputPath.ServerTest, dir = config.applicationPackage, key = "TestServices")

    config.addCommonImport(file, "TracingService")
    config.addCommonImport(file, "JdbcDatabase")
    config.addCommonImport(file, "ExecutionContext")

    file.add(s"object TestServices {", 1)
    file.add("private[this] implicit val ec: ExecutionContext = ExecutionContext.global")
    file.add("val trace = TracingService.noop")
    file.add("""lazy val db = new JdbcDatabase("application", "database.application") {}""")

    file.add()
    models.foreach { model =>
      val fullSvc = (model.servicePackage(config) :+ (model.className + "Service")).mkString(".")
      file.add(s"lazy val ${model.propertyName}Service = new $fullSvc(db, trace)")
    }
    file.add("}", -1)

    file
  }

  private[this] def exportTestModelSupport(config: ExportConfiguration, models: Seq[ExportModel]) = {
    val file = ScalaFile(path = OutputPath.ServerTest, dir = config.applicationPackage, key = "TestModelSupport")

    config.addCommonImport(file, "Credentials")
    config.addCommonImport(file, "DataFieldModel")
    config.addCommonImport(file, "TraceData")

    file.add(s"object TestModelSupport {", 1)
    file.add("private[this] val creds = Credentials.noop")
    file.add("private[this] implicit val td: TraceData = TraceData.noop")
    file.add()
    file.add("def insert(m: DataFieldModel) = m match {", 1)
    models.foreach { model =>
      val fullPath = (model.modelPackage(config) :+ model.className).mkString(".")
      file.add(s"case model: $fullPath => TestServices.${model.propertyName}Service.insert(creds, model)")
    }
    file.add("""case model => throw new IllegalStateException(s"Unable to insert unhandled model [$model]")""")
    file.add("}", -1)
    file.add()
    file.add("def remove(m: DataFieldModel) = m match {", 1)
    models.foreach { model =>
      val fullPath = (model.modelPackage(config) :+ model.className).mkString(".")
      val pkFields = model.pkFields.map(f => s"model.${f.propertyName}").mkString(", ")
      file.add(s"case model: $fullPath => TestServices.${model.propertyName}Service.remove(creds, $pkFields)")
    }
    file.add("""case model => throw new IllegalStateException(s"Unable to remove unhandled model [$model]")""")
    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
