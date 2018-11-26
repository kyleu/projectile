package com.projectile.models.database.input

import java.sql.DriverManager
import java.util.Properties

import com.projectile.models.database.schema.{EnumType, Table, View}
import com.projectile.models.export.ExportEnum
import com.projectile.models.input.{Input, InputTemplate}
import com.projectile.models.output.ExportHelper
import com.projectile.models.project.member.EnumMember.InputType
import com.projectile.util.JsonSerializers._

object PostgresInput {
  implicit val jsonEncoder: Encoder[PostgresInput] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresInput] = deriveDecoder
}

case class PostgresInput(
    override val key: String = "new",
    override val title: String = "New Postgres Imput",
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

  def getEnum(k: String) = enums.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"Cannot find enum [$k] in input [$key] among candidates [${enums.map(_.key).mkString(", ")}]")
  }

  def getTable(k: String) = tables.find(_.name == k).getOrElse {
    throw new IllegalStateException(s"Cannot find table [$k] in input [$key] among candidates [${tables.map(_.name).mkString(", ")}]")
  }

  def getView(k: String) = views.find(_.name == k).getOrElse {
    throw new IllegalStateException(s"Cannot find view [$k] in input [$key] among candidates [${views.map(_.name).mkString(", ")}]")
  }

  override def exportEnum(key: String) = {
    val e = getEnum(key)
    ExportEnum(inputType = InputType.PostgresEnum, key = e.key, className = ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)), values = e.values)
  }

  override lazy val exportEnums = enums.map(e => exportEnum(e.key))

  override def exportModel(key: String) = {
    tables.find(_.name == key) match {
      case Some(table) => TableExportModel.loadTableModel(table, tables, exportEnums)
      case None => views.find(_.name == key) match {
        case Some(view) => ViewExportModel.loadViewModel(view, exportEnums)
        case None => throw new IllegalStateException(s"Cannot find view or table [$key] in input [$key]")
      }
    }
  }

  override lazy val exportModels = tables.map(e => exportModel(e.name))

  override def exportService(key: String) = throw new IllegalStateException("Services not supported for Postgres inputs")

  override def exportServices = Nil

  def newConnection() = {
    val props = new Properties()
    props.setProperty("user", username)
    props.setProperty("password", password)
    props.setProperty("ssl", ssl.toString)
    DriverManager.getConnection(url, props)
  }
}
