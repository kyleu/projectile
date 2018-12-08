package com.projectile.models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class EnumInputType(override val value: String, val isDatabase: Boolean = false, val isGraphQL: Boolean = false) extends StringEnumEntry {
  override val toString = value
}

object EnumInputType extends StringEnum[EnumInputType] with StringCirceEnum[EnumInputType] {
  case object PostgresEnum extends EnumInputType(value = "postgres-enum", isDatabase = true)
  case object ThriftIntEnum extends EnumInputType(value = "thrift-int-enum")
  case object ThriftStringEnum extends EnumInputType(value = "thrift-string-enum")
  case object GraphQLEnum extends EnumInputType(value = "graphql-enum", isGraphQL = true)

  override val values = findValues
}
