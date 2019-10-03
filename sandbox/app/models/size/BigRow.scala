/* Generated File */
package models.size

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.JsonSerializers._

object BigRow {
  implicit val jsonEncoder: Encoder[BigRow] = (r: BigRow) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("t", r.t.asJson)
  )

  implicit val jsonDecoder: Decoder[BigRow] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[Long]
    t <- c.downField("t").as[Option[String]]
  } yield BigRow(id, t)

  def empty(
    id: Long = 0L,
    t: Option[String] = None
  ) = {
    BigRow(id, t)
  }
}

final case class BigRow(
    id: Long,
    t: Option[String]
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("t", t)
  )

  def toSummary = DataSummary(model = "bigRow", pk = id.toString, entries = Map(
    "Id" -> Some(id.toString),
    "T" -> t
  ))
}
