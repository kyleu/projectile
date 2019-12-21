package com.kyleu.projectile.models.result.orderBy

import com.kyleu.projectile.util.JsonSerializers._
import enumeratum._

object OrderBy {
  implicit val jsonEncoder: Encoder[OrderBy] = deriveEncoder
  implicit val jsonDecoder: Decoder[OrderBy] = deriveDecoder

  sealed abstract class Direction(val sql: String, val asBool: Boolean) extends EnumEntry

  object Direction extends Enum[Direction] with CirceEnum[Direction] {
    case object Ascending extends Direction("asc", true)
    case object Descending extends Direction("desc", false)

    def fromBoolAsc(b: Boolean): Direction = if (b) { Ascending } else { Descending }
    override val values = findValues
  }

  def forVals(col: Option[String], asc: Boolean, default: Option[(String, Boolean)]) = {
    col.map(c => OrderBy(col = c, dir = OrderBy.Direction.fromBoolAsc(asc))).orElse(default.map {
      case (c, a) => OrderBy(col = c, dir = OrderBy.Direction.fromBoolAsc(a))
    })
  }
}

final case class OrderBy(
    col: String = "?",
    dir: OrderBy.Direction = OrderBy.Direction.Ascending
)
