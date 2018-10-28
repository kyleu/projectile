package models.output.feature.service

import models.database.schema.ColumnType
import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.output.file.ScalaFile

object QueriesHelper {
  private[this] val columnPropertyIds = Map(
    "name" -> "nameArg"
  )

  def fromRow(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    file.add(s"override def fromRow(row: Row) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      if (field.notNull) {
        file.add(s"""${field.propertyName} = ${field.classNameForSqlType(config)}(row, "${field.columnName}")$comma""")
      } else {
        file.add(s"""${field.propertyName} = ${field.classNameForSqlType(config)}.opt(row, "${field.columnName}")$comma""")
      }
    }
    file.add(")", -1)
  }

  def writeForeignKeys(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val fkCols = model.foreignKeys.flatMap { fk =>
      fk.references match {
        case ref :: Nil => Some(ref.source)
        case _ => None
      }
    }
    val cols = (fkCols ++ model.searchFields.map(_.columnName)).distinct.sorted
    cols.foreach(col => addColQueriesToFile(config, model, file, col))
  }

  private[this] def addColQueriesToFile(config: ExportConfiguration, model: ExportModel, file: ScalaFile, col: String) = {
    file.addImport((config.systemPackage ++ Seq("models", "queries")).mkString("."), "ResultFieldHelper")
    file.addImport((config.systemPackage ++ Seq("models", "result", "orderBy")).mkString("."), "OrderBy")

    val field = model.fields.find(_.columnName == col).getOrElse(throw new IllegalStateException(s"Missing column [$col]."))
    field.addImport(config, file, Nil)
    val propId = columnPropertyIds.getOrElse(field.propertyName, field.propertyName)
    val propCls = field.className
    field.t match {
      case ColumnType.TagsType => file.addImport(config.tagsPackage.mkString("."), "Tag")
      case _ => // noop
    }
    val ft = field.scalaType(config)
    file.add(s"""final case class CountBy$propCls($propId: $ft) extends ColCount(column = "${field.columnName}", values = Seq($propId))""")
    val searchArgs = "orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"
    file.add(s"""final case class GetBy$propCls($propId: $ft, $searchArgs) extends SeqQuery(""", 1)
    file.add(s"""whereClause = Some(quote("${field.columnName}") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),""")
    file.add(s"limit = limit, offset = offset, values = Seq($propId)")
    file.add(")", -1)
    val sig = s"GetBy${propCls}Seq(${propId}Seq: Seq[$ft])"
    file.add(s"""final case class $sig extends ColSeqQuery(column = "${field.columnName}", values = ${propId}Seq)""")
    file.add()
  }

}
