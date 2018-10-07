package models.input

import util.JsonSerializers._

object PostgresInput {
  val t = "postgres"

  implicit val jsonEncoder: Encoder[PostgresInput] = deriveEncoder
  implicit val jsonDecoder: Decoder[PostgresInput] = deriveDecoder
}

case class PostgresInput(
    override val key: String,
    override val title: String,
    override val description: String
) extends Input {
  override val t = PostgresInput.t
}
