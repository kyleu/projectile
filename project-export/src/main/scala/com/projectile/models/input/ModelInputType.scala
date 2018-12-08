package com.projectile.models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ModelInputType(override val value: String, val isDatabase: Boolean = false, val isGraphQL: Boolean = false) extends StringEnumEntry {
  override val toString = value
}

object ModelInputType extends StringEnum[ModelInputType] with StringCirceEnum[ModelInputType] {
  case object PostgresTable extends ModelInputType(value = "postgres-table", isDatabase = true)
  case object PostgresView extends ModelInputType(value = "postgres-view", isDatabase = true)
  case object ThriftStruct extends ModelInputType(value = "thrift-struct")
  case object GraphQLFragment extends ModelInputType(value = "graphql-fragment", isGraphQL = true)
  case object GraphQLInput extends ModelInputType(value = "graphql-input", isGraphQL = true)
  case object GraphQLMutation extends ModelInputType(value = "graphql-mutation", isGraphQL = true)
  case object GraphQLQuery extends ModelInputType(value = "graphql-query", isGraphQL = true)

  override val values = findValues
}
