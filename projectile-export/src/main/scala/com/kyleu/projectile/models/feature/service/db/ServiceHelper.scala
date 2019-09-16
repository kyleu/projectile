// scalastyle:off file.size.limit
package com.kyleu.projectile.models.feature.service.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceHelper {
  val conn = "conn: Option[Connection] = None"

  def writeSearchFields(model: ExportModel, file: ScalaFile, queriesFile: String, trace: String, searchArgs: String, viewCheck: String) = {
    file.add(s"""override def countAll(creds: Credentials, filters: Seq[Filter] = Nil, $conn)$trace = $viewCheck{""", 1)
    file.add(s"""traceF("get.all.count")(td => db.queryF($queriesFile.countAll(filters), conn)(td))""")
    file.add("}", -1)
    file.add(s"""override def getAll(creds: Credentials, $searchArgs, $conn)$trace = $viewCheck{""", 1)
    file.add(s"""traceF("get.all")(td => db.queryF($queriesFile.getAll(filters, orderBys, limit, offset), conn)(td))""")
    file.add("}", -1)
    file.add()
    file.add("// Search")
    file.add(s"""override def searchCount(creds: Credentials, q: Option[String], filters: Seq[Filter] = Nil, $conn)$trace = $viewCheck{""", 1)
    file.add(s"""traceF("search.count")(td => db.queryF($queriesFile.searchCount(q, filters), conn)(td))""")
    file.add("}", -1)
    file.add("override def search(")
    file.add(s"  creds: Credentials, q: Option[String], $searchArgs, $conn")
    file.add(s""")$trace = $viewCheck{""", 1)
    file.add(s"""traceF("search")(td => db.queryF($queriesFile.search(q, filters, orderBys, limit, offset), conn)(td))""")
    file.add("}", -1)
    file.add()
    file.add("def searchExact(")
    file.add(s"  creds: Credentials, q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None, $conn")
    file.add(s""")$trace = $viewCheck{""", 1)
    file.add(s"""traceF("search.exact")(td => db.queryF($queriesFile.searchExact(q, orderBys, limit, offset), conn)(td))""")
    file.add("}", -1)
    file.add()
  }

  private[this] val td = "(implicit trace: TraceData)"

  def addGetters(config: ExportConfiguration, model: ExportModel, file: ScalaFile, viewCheck: String) = {
    model.pkFields.foreach(_.addImport(config, file, Nil))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        val colProp = field.propertyName
        file.add(s"""def getByPrimaryKey(creds: Credentials, $colProp: ${field.scalaType(config)}, $conn)$td = $viewCheck{""", 1)
        file.add(s"""traceF("get.by.primary.key")(td => db.queryF(${model.className}Queries.getByPrimaryKey($colProp), conn)(td))""")
        file.add("}", -1)

        val call = s"getByPrimaryKey(creds, $colProp, conn)"
        file.add(s"""def getByPrimaryKeyRequired(creds: Credentials, $colProp: ${field.scalaType(config)}, $conn)$td = $call.map { opt =>""", 1)
        file.add(s"""opt.getOrElse(throw new IllegalStateException(s"Cannot load ${model.propertyName} with $colProp [$$$colProp]"))""")
        file.add("}", -1)

        val seqArgs = s"${colProp}Seq: Seq[${field.scalaType(config)}]"
        file.add(s"""def getByPrimaryKeySeq(creds: Credentials, $seqArgs, $conn)$td = $viewCheck{""", 1)
        file.add(s"if (${colProp}Seq.isEmpty) {", 1)
        file.add("Future.successful(Nil)")
        file.add("} else {", -1)
        file.indent()
        file.add(s"""traceF("get.by.primary.key.seq")(td => db.queryF(${model.className}Queries.getByPrimaryKeySeq(${colProp}Seq), conn)(td))""")
        file.add("}", -1)
        file.add("}", -1)
      case fields => // multiple columns
        val tupleTyp = "(" + fields.map(_.scalaType(config)).mkString(", ") + ")"
        val colArgs = fields.map(f => f.propertyName + ": " + f.scalaType(config)).mkString(", ")
        val queryArgs = fields.map(_.propertyName).mkString(", ")

        file.add(s"""def getByPrimaryKey(creds: Credentials, $colArgs, $conn)$td = $viewCheck{""", 1)
        file.add(s"""traceF("get.by.primary.key")(td => db.queryF(${model.className}Queries.getByPrimaryKey($queryArgs), conn)(td))""")
        file.add("}", -1)

        file.add(s"""def getByPrimaryKeySeq(creds: Credentials, pkSeq: Seq[$tupleTyp], $conn)$td = $viewCheck{""", 1)
        file.add("if (pkSeq.isEmpty) {", 1)
        file.add("Future.successful(Nil)")
        file.add("} else {", -1)
        file.indent()
        file.add(s"""traceF("get.by.primary.key.seq")(td => db.queryF(${model.className}Queries.getByPrimaryKeySeq(pkSeq), conn)(td))""")
        file.add("}", -1)
        file.add("}", -1)
    }
    file.add()
  }

  def writeForeignKeys(config: ExportConfiguration, model: ExportModel, file: ScalaFile, viewCheck: String) = {
    model.searchCols.foreach(col => addRelationMethodsToFile(config, model, file, col, viewCheck))
  }

  private[this] def addRelationMethodsToFile(config: ExportConfiguration, model: ExportModel, file: ScalaFile, col: String, viewCheck: String) = {
    val searchArgs = "orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"
    val field = model.fields.find(_.key == col).getOrElse(throw new IllegalStateException(s"Missing column [$col]"))
    field.addImport(config, file, Nil)

    val propId = field.propertyName
    val propCls = field.className

    file.add(s"""def countBy$propCls(creds: Credentials, $propId: ${field.scalaType(config)}, $conn)(implicit trace: TraceData) = $viewCheck{""", 1)
    file.add(s"""traceF("count.by.$propId")(td => db.queryF(${model.className}Queries.CountBy$propCls($propId), conn)(td))""")
    file.add("}", -1)
    val fkArgs = s"creds: Credentials, $propId: ${field.scalaType(config)}, $searchArgs"
    file.add(s"""def getBy$propCls($fkArgs, $conn)(implicit trace: TraceData) = $viewCheck{""", 1)
    file.add(s"""traceF("get.by.$propId")(td => db.queryF(${model.className}Queries.GetBy$propCls($propId, orderBys, limit, offset), conn)(td))""")
    file.add("}", -1)
    val fkSeqArgs = s"creds: Credentials, ${propId}Seq: Seq[${field.scalaType(config)}]"
    file.add(s"""def getBy${propCls}Seq($fkSeqArgs, $conn)(implicit trace: TraceData) = $viewCheck{""", 1)
    file.add(s"if (${propId}Seq.isEmpty) {", 1)
    file.add("Future.successful(Nil)")
    file.add("} else {", -1)
    file.indent()
    file.add(s"""traceF("get.by.$propId.seq") { td =>""", 1)
    file.add(s"db.queryF(${model.className}Queries.GetBy${propCls}Seq(${propId}Seq), conn)(td)")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)

    file.add()
  }
}
