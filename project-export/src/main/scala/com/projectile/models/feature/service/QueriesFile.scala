package com.projectile.models.feature.service

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.OutputPath
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.file.ScalaFile

object QueriesFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.queriesPackage, key = model.className + "Queries")

    file.addImport(config.applicationPackage ++ model.modelPackage, model.className)
    file.addImport(config.systemPackage ++ Seq("models", "database"), "Row")
    file.addImport(config.systemPackage ++ Seq("models", "database"), "DatabaseField")
    file.addImport(config.systemPackage ++ Seq("models", "database", "DatabaseFieldType"), "_")

    if (model.pkg.nonEmpty) {
      file.addImport(config.systemPackage ++ Seq("models", "queries"), "BaseQueries")
    }

    file.add(s"""object ${model.className}Queries extends BaseQueries[${model.className}]("${model.propertyName}", "${model.key}") {""", 1)
    file.add("override val fields = Seq(", 1)
    model.fields.foreach { f =>
      f.addImport(config, file, Nil)
      val field = s"""DatabaseField(title = "${f.title}", prop = "${f.propertyName}", col = "${f.key}", typ = ${f.classNameForSqlType(config)})"""
      val comma = if (model.fields.lastOption.contains(f)) { "" } else { "," }
      file.add(field + comma)
    }
    file.add(")", -1)

    if (model.pkFields.nonEmpty) {
      file.add("override protected val pkColumns = Seq(" + model.pkFields.map("\"" + _.key + "\"").mkString(", ") + ")")
      file.add(s"override protected val searchColumns = Seq(${model.searchFields.map("\"" + _.key + "\"").mkString(", ")})")
    }
    file.add()

    file.addImport(config.systemPackage ++ Seq("models", "result", "filter"), "Filter")
    file.add("def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)")

    file.add("def getAll(filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {", 1)
    file.add("new GetAll(filters, orderBys, limit, offset)")
    file.add("}", -1)
    file.add()

    val searchArgs = "q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"
    file.add(s"def search($searchArgs) = {", 1)
    file.add("new Search(q, filters, orderBys, limit, offset)")
    file.add("}", -1)
    file.add("def searchCount(q: Option[String], filters: Seq[Filter] = Nil) = new SearchCount(q, filters)")
    file.add("def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = new SearchExact(q, orderBys, limit, offset)")
    file.add()

    writePkFields(config, file, model)

    QueriesHelper.writeForeignKeys(config, model, file)

    if (!model.readOnly) {
      file.add(s"def insert(model: ${model.className}) = new Insert(model)")
      file.add(s"def insertBatch(models: Seq[${model.className}]) = new InsertBatch(models)")

      file.addImport(config.systemPackage ++ Seq("models", "result", "data"), "DataField")
      file.add("def create(dataFields: Seq[DataField]) = new CreateFields(dataFields)")
    }

    if (model.pkFields.nonEmpty) {
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.scalaType(config)).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      file.add()
      file.add(s"def removeByPrimaryKey($sig) = new RemoveByPrimaryKey(Seq[Any]($call))")
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField]) = new UpdateFields(Seq[Any]($call), fields)")
    }

    file.add()
    QueriesHelper.fromRow(config, model, file)

    file.add("}", -1)
    file
  }

  private[this] def writePkFields(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = model.pkFields match {
    case Nil => // noop
    case pkField :: Nil =>
      val name = pkField.propertyName
      pkField.addImport(config, file, Nil)
      file.add(s"def getByPrimaryKey($name: ${model.pkType(config)}) = new GetByPrimaryKey(Seq($name))")
      file.add(s"""def getByPrimaryKeySeq(${name}Seq: Seq[${model.pkType(config)}]) = new ColSeqQuery(column = "${pkField.key}", values = ${name}Seq)""")
      file.add()
    case pkFields =>
      pkFields.foreach(_.addImport(config, file, Nil))
      val args = pkFields.map(x => s"${x.propertyName}: ${x.scalaType(config)}").mkString(", ")
      val seqArgs = pkFields.map(_.propertyName).mkString(", ")
      file.add(s"def getByPrimaryKey($args) = new GetByPrimaryKey(Seq[Any]($seqArgs))")
      file.add(s"def getByPrimaryKeySeq(idSeq: Seq[${model.pkType(config)}]) = new SeqQuery(", 1)
      val pkWhere = pkFields.map(f => "\\\"" + f.key + "\\\" = ?").mkString(" and ")
      file.add(s"""whereClause = Some(idSeq.map(_ => "($pkWhere)").mkString(" or ")),""")
      file.add("values = idSeq.flatMap(_.productIterator.toSeq)")
      file.add(")", -1)
      file.add()
  }
}
