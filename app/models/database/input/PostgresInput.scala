package models.database.input

import java.sql.DriverManager
import java.util.Properties

import models.database.schema.{EnumType, Table, View}
import models.input.{Input, InputTemplate}
import util.JsonSerializers._

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

  def newConnection() = {
    val props = new Properties()
    props.setProperty("user", username)
    props.setProperty("password", password)
    props.setProperty("ssl", ssl.toString)
    DriverManager.getConnection(url, props)
  }
}
