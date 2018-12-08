package com.projectile.models.project.member

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.projectile.models.feature.ModelFeature
import com.projectile.util.JsonSerializers._

object ModelMember {
  sealed abstract class InputType(override val value: String, val isDatabase: Boolean = false, val isGraphQL: Boolean = false) extends StringEnumEntry

  object InputType extends StringEnum[InputType] with StringCirceEnum[InputType] {
    case object PostgresTable extends InputType(value = "postgres-table", isDatabase = true)
    case object PostgresView extends InputType(value = "postgres-view", isDatabase = true)
    case object ThriftStruct extends InputType(value = "thrift-struct")
    case object GraphQLFragment extends InputType(value = "graphql-fragment", isGraphQL = true)
    case object GraphQLInput extends InputType(value = "graphql-input", isGraphQL = true)
    case object GraphQLMutation extends InputType(value = "graphql-mutation", isGraphQL = true)
    case object GraphQLQuery extends InputType(value = "graphql-query", isGraphQL = true)

    override val values = findValues
  }

  implicit val jsonEncoder: Encoder[ModelMember] = deriveEncoder
  implicit val jsonDecoder: Decoder[ModelMember] = deriveDecoder
}

case class ModelMember(
    input: String,
    inputType: ModelMember.InputType,

    key: String,
    pkg: Seq[String] = Nil,

    features: Set[ModelFeature] = Set.empty,
    ignored: Set[String] = Set.empty,
    overrides: Seq[MemberOverride] = Nil
) {
  def getOverride(key: String, default: => String) = overrides.find(_.k == key).map(_.v).getOrElse(default)
}
