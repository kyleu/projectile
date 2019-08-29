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
    host: String = "localhost",
    port: Int = 5432,
    username: String = "",
    password: String = "",
    db: String = "",
    ssl: Boolean = false,
    catalog: Option[String] = None,
    enumTypes: Seq[EnumType] = Nil,
    tables: Seq[Table] = Nil,
    views: Seq[View] = Nil
) extends Input {
  override def template = InputTemplate.Postgres

  val url: String = s"jdbc:postgresql://$host:$port/$db"

  override lazy val enums = enumTypes.map(e => ExportEnum(
    inputType = InputType.Enum.PostgresEnum,
    pkg = Nil,
    key = e.key,
    className = PostgresInput.typeName(ExportHelper.toClassName(ExportHelper.toIdentifier(e.key))),
    values = e.values.map(v => ExportEnum.EnumVal(k = v, s = Some(v)))
  ))

  override lazy val models = {
    val t = tables.map(table => TableExportModel.loadTableModel(table, tables, enums))
    val v = Nil // ViewExportModel.loadViewModel(view, exportEnums)
    t ++ v
  }

  def newConnection() = PostgresInputDataSourceOverrides.get(key).map(_().getConnection()).getOrElse {
    Class.forName("org.postgresql.Driver")
    val props = new Properties()
    props.setProperty("user", username)
    props.setProperty("password", password)
    props.setProperty("ssl", ssl.toString)
    val url = s"jdbc:postgresql://$host:$port/$db"
    DriverManager.getConnection(url, props)
  }
}
