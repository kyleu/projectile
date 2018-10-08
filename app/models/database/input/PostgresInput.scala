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
    override val key: String,
    override val title: String = "New Postgres Imput",
    override val description: String = "...",
    url: String = "jdbc:postgresql://localhost/db",
    username: String,
    password: String,
    ssl: Boolean = false,
    db: String,
    catalog: Option[String],
    enums: Seq[EnumType],
    tables: Seq[Table],
    views: Seq[View],
) extends Input {
  override def t = InputTemplate.Postgres

  def newConnection() = {
    val props = new Properties()
    props.setProperty("user", username)
    props.setProperty("password", password)
    props.setProperty("ssl", ssl.toString)
    DriverManager.getConnection(url, props)
  }
}
