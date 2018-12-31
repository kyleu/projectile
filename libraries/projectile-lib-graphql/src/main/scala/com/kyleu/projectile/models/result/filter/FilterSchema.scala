package com.kyleu.projectile.models.result.filter

import com.kyleu.projectile.graphql.CommonSchema
import com.kyleu.projectile.graphql.GraphQLContext
import sangria.macros.derive._
import sangria.schema._
import sangria.marshalling.circe._
import com.kyleu.projectile.models.result.data.DataFieldSchema

object FilterSchema {
  implicit val filterOpType: EnumType[FilterOp] = CommonSchema.deriveEnumeratumType[FilterOp](
    name = "FilterOperation",
    values = FilterOp.values
  )

  implicit val filterType: ObjectType[GraphQLContext, Filter] = deriveObjectType[GraphQLContext, Filter]()

  val filterInputType = InputObjectType[Filter](name = "FilterInput", fields = List(
    InputField("k", StringType),
    InputField("o", filterOpType),
    InputField("v", ListInputType(DataFieldSchema.varType))
  ))

  val reportFiltersArg = Argument("filters", OptionInputType(ListInputType(filterInputType)))
}
