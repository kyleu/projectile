package com.projectile.models.output.feature.service

import com.projectile.models.database.schema.ColumnType
import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.ScalaFile

object QueriesHelper {
  private[this] val columnPropertyIds = Map(
    "name" -> "nameArg"
  )

  def fromRow(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    file.add(s"override def fromRow(row: Row) = ${model.className}(", 1)
    model.fields.foreach { field =>
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      if (field.notNull) {
        file.add(s"""${field.propertyName} = ${field.classNameForSqlType(config)}(row, "${field.key}")$comma""")
      } else {
        file.add(s"""${field.propertyName} = ${field.classNameForSqlType(config)}.opt(row, "${field.key}")$comma""")
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
    val cols = (fkCols ++ model.searchFields.map(_.key)).distinct.sorted
    cols.foreach(col => addColQueriesToFile(config, model, file, col))
  }

  private[this] def addColQueriesToFile(config: ExportConfiguration, model: ExportModel, file: ScalaFile, col: String) = {
    config.addCommonImport(file, "ResultFieldHelper")
    config.addCommonImport(file, "OrderBy")

    val field = model.fields.find(_.key == col).getOrElse(throw new IllegalStateException(s"Missing column [$col]."))
    field.addImport(config, file, Nil)
    val propId = columnPropertyIds.getOrElse(field.propertyName, field.propertyName)
    val propCls = field.className
    field.t match {
      case ColumnType.TagsType => config.addCommonImport(file, "Tag")
      case _ => // noop
    }
    val ft = field.scalaType(config)
    file.add(s"""final case class CountBy$propCls($propId: $ft) extends ColCount(column = "${field.key}", values = Seq($propId))""")
    val searchArgs = "orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"
    file.add(s"""final case class GetBy$propCls($propId: $ft, $searchArgs) extends SeqQuery(""", 1)
    file.add(s"""whereClause = Some(quote("${field.key}") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),""")
    file.add(s"limit = limit, offset = offset, values = Seq($propId)")
    file.add(")", -1)
    val sig = s"GetBy${propCls}Seq(${propId}Seq: Seq[$ft])"
    file.add(s"""final case class $sig extends ColSeqQuery(column = "${field.key}", values = ${propId}Seq)""")
    file.add()
  }

}
