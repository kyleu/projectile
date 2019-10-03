/* Generated File */
package models.size

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.JsonSerializers._

object SmallRow {
  implicit val jsonEncoder: Encoder[SmallRow] = (r: SmallRow) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("bigId", r.bigId.asJson),
    ("t", r.t.asJson)
  )

  implicit val jsonDecoder: Decoder[SmallRow] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[Long]
    bigId <- c.downField("bigId").as[Long]
    t <- c.downField("t").as[Option[String]]
  } yield SmallRow(id, bigId, t)

  def empty(
    id: Long = 0L,
    bigId: Long = 0L,
    t: Option[String] = None
  ) = {
    SmallRow(id, bigId, t)
  }
}

final case class SmallRow(
    id: Long,
    bigId: Long,
    t: Option[String]
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("bigId", Some(bigId.toString)),
    DataField("t", t)
  )

  def toSummary = DataSummary(model = "smallRow", pk = id.toString, entries = Map(
    "Id" -> Some(id.toString),
    "Big Id" -> Some(bigId.toString),
    "T" -> t
  ))
}
