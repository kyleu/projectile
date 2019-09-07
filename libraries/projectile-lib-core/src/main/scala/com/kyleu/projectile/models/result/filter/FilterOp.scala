package com.kyleu.projectile.models.result.filter

import enumeratum._

sealed abstract class FilterOp extends EnumEntry {
  def vals(v: Seq[String]) = v
}

object FilterOp extends Enum[FilterOp] with CirceEnum[FilterOp] {
  case object Equal extends FilterOp
  case object NotEqual extends FilterOp
  case object Like extends FilterOp
  case object GreaterThanOrEqual extends FilterOp
  case object LessThanOrEqual extends FilterOp
  case object IsNull extends FilterOp
  case object IsNotNull extends FilterOp

  override val values = findValues
}
