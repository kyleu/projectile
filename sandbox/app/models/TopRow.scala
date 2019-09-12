/* Generated File */
package models

import com.kyleu.projectile.models.result.data.{DataField, DataFieldModel, DataSummary}
import com.kyleu.projectile.util.JsonSerializers._
import java.util.UUID

object TopRow {
  implicit val jsonEncoder: Encoder[TopRow] = (r: TopRow) => io.circe.Json.obj(
    ("id", r.id.asJson),
    ("t", r.t.asJson)
  )

  implicit val jsonDecoder: Decoder[TopRow] = (c: io.circe.HCursor) => for {
    id <- c.downField("id").as[UUID]
    t <- c.downField("t").as[Option[String]]
  } yield TopRow(id, t)

  def empty(
    id: UUID = UUID.randomUUID,
    t: Option[String] = None
  ) = {
    TopRow(id, t)
  }
}

final case class TopRow(
    id: UUID,
    t: Option[String]
) extends DataFieldModel {
  override def toDataFields = Seq(
    DataField("id", Some(id.toString)),
    DataField("t", t)
  )

  def toSummary = DataSummary(model = "topRow", pk = id.toString, title = s"id: $id, t: ${t.map(_.toString).getOrElse("∅")}")
}
