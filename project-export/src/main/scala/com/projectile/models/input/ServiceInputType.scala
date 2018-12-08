package com.projectile.models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ServiceInputType(override val value: String) extends StringEnumEntry {
  override val toString = value
}

object ServiceInputType extends StringEnum[ServiceInputType] with StringCirceEnum[ServiceInputType] {
  case object ThriftService extends ServiceInputType(value = "thrift-service")

  override val values = findValues
}
