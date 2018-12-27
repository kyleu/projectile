package com.kyleu.projectile.models.input

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

object InputType {

  sealed abstract class Enum(
      override val value: String,
      val isDatabase: Boolean = false,
      val isGraphQL: Boolean = false,
      val isThrift: Boolean = false
  ) extends StringEnumEntry {
    override val toString = value
  }

  object Enum extends StringEnum[Enum] with StringCirceEnum[Enum] {
    case object PostgresEnum extends Enum(value = "postgres-enum", isDatabase = true)
    case object ThriftIntEnum extends Enum(value = "thrift-int-enum", isThrift = true)
    case object ThriftStringEnum extends Enum(value = "thrift-string-enum", isThrift = true)
    case object GraphQLEnum extends Enum(value = "graphql-enum", isGraphQL = true)

    override val values = findValues
  }

  sealed abstract class Model(
      override val value: String,
      val isDatabase: Boolean = false,
      val isThrift: Boolean = false,
      val isGraphQL: Boolean = false
  ) extends StringEnumEntry {
    override val toString = value
  }

  object Model extends StringEnum[Model] with StringCirceEnum[Model] {
    case object PostgresTable extends Model(value = "postgres-table", isDatabase = true)
    case object PostgresView extends Model(value = "postgres-view", isDatabase = true)
    case object ThriftStruct extends Model(value = "thrift-struct", isThrift = true)
    case object GraphQLFragment extends Model(value = "graphql-fragment", isGraphQL = true)
    case object GraphQLInput extends Model(value = "graphql-input", isGraphQL = true)
    case object GraphQLMutation extends Model(value = "graphql-mutation", isGraphQL = true)
    case object GraphQLQuery extends Model(value = "graphql-query", isGraphQL = true)
    case object GraphQLReference extends Model(value = "graphql-reference", isGraphQL = true)

    override val values = findValues
  }

  sealed abstract class Service(
      override val value: String,
      val isDatabase: Boolean = false,
      val isThrift: Boolean = false,
      val isGraphQL: Boolean = false
  ) extends StringEnumEntry {
    override val toString = value
  }

  object Service extends StringEnum[Service] with StringCirceEnum[Service] {
    case object ThriftService extends Service(value = "thrift-service", isThrift = true)

    override val values = findValues
  }
}
