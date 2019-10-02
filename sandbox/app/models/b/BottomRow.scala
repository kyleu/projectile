/* Generated File */
package models.b

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.JsonSerializers._
import java.util.UUID

object BottomRow {
  implicit val jsonEncoder: Encoder[BottomRow] = (r: BottomRow) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("topId", r.topId.asJson),
    ("t", r.t.asJson)
  )

  implicit val jsonDecoder: Decoder[BottomRow] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[UUID]
    topId <- c.downField("topId").as[UUID]
    t <- c.downField("t").as[Option[String]]
  } yield BottomRow(id, topId, t)

  def empty(
    id: UUID = UUID.randomUUID,
    topId: UUID = UUID.randomUUID,
    t: Option[String] = None
  ) = {
    BottomRow(id, topId, t)
  }
}

final case class BottomRow(
    id: UUID,
    topId: UUID,
    t: Option[String]
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("topId", Some(topId.toString)),
    DataField("t", t)
  )

  def toSummary = DataSummary(model = "bottomRow", pk = id.toString, entries = Map(
    "Id" -> Some(id.toString),
    "Top Id" -> Some(topId.toString),
    "T" -> t
  ))
}
