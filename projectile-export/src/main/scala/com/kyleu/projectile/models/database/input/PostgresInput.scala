package com.kyleu.projectile.models.database.input

import java.sql.DriverManager
import java.util.Properties

import com.kyleu.projectile.models.database.schema.{EnumType, Table, View}
import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.input.{InputType, Input, InputTemplate}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.util.JsonSerializers._

object PostgresInput {
  implicit val jsonEncoder: Encoder[PostgresInput] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresInput] = deriveDecoder

  def rowName(s: String) = if (s.endsWith("Row")) { s } else { s + "Row" }
  def typeName(s: String) = if (s.endsWith("Type")) { s } else { s + "Type" }
}

case class PostgresInput(
    override val key: String = "new",
    override val description: String = "...",
    url: String = "jdbc:postgresql://localhost/db",
    username: String = "",
    password: String = "",
    ssl: Boolean = false,
    db: String = "db",
    catalog: Option[String] = None,
    enums: Seq[EnumType] = Nil,
    tables: Seq[Table] = Nil,
    views: Seq[View] = Nil
) extends Input {
  override def template = InputTemplate.Postgres

  private[this] def getPostgresEnum(k: String) = enums.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"Cannot find enum [$k] in input [$key] among candidates [${enums.map(_.key).mkString(", ")}]")
  }

  override def exportEnum(key: String) = {
    val e = getPostgresEnum(key)
    ExportEnum(
      inputType = InputType.Enum.PostgresEnum,
      key = e.key,
      className = PostgresInput.typeName(ExportHelper.toClassName(ExportHelper.toIdentifier(e.key))),
      values = e.values
    )
  }

  override lazy val exportEnums = enums.map(e => exportEnum(e.key))

  override def exportModel(k: String) = {
    tables.find(_.name == k) match {
      case Some(table) => TableExportModel.loadTableModel(table, tables, exportEnums)
      case None => views.find(_.name == k) match {
        case Some(view) => ViewExportModel.loadViewModel(view, exportEnums)
        case None => throw new IllegalStateException(s"Cannot find view or table [$k] in input [$key]")
      }
    }
  }

  override lazy val exportModels = tables.map(e => exportModel(e.name))

  override def exportService(k: String) = throw new IllegalStateException("Services not supported for Postgres inputs")

  override def exportServices = Nil

  def newConnection() = {
    Class.forName("org.postgresql.Driver")
    val props = new Properties()
    props.setProperty("user", username)
    props.setProperty("password", password)
    props.setProperty("ssl", ssl.toString)
    DriverManager.getConnection(url, props)
  }
}
