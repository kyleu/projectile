package com.kyleu.projectile.graphql

import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}
import java.util.UUID

import io.circe.{Decoder, Encoder, Json}
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.result.paging.PagingOptions
import com.kyleu.projectile.models.tag.Tag
import sangria.macros.derive._
import sangria.marshalling.{FromInput, ToInput}
import sangria.schema.{EnumType, InputObjectType, ObjectType, ScalarType}

import scala.concurrent.ExecutionContext
import scala.language.experimental.macros

object GraphQLUtils {
  type TraceData = com.kyleu.projectile.util.tracing.TraceData
  type Context[Ctx, Val] = sangria.schema.Context[Ctx, Val]

  type GraphQLField = sangria.macros.derive.GraphQLField

  implicit val ctx: ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit def circeDecoderFromInput[T: Decoder]: FromInput[T] = sangria.marshalling.circe.circeDecoderFromInput
  implicit def circeEncoderToInput[T: Encoder]: ToInput[T, Json] = sangria.marshalling.circe.circeEncoderToInput

  val queryArg = CommonSchema.queryArg
  val limitArg = CommonSchema.limitArg
  val offsetArg = CommonSchema.offsetArg
  val dataFieldsArg = com.kyleu.projectile.models.result.data.DataFieldSchema.dataFieldsArg

  val reportFiltersArg = com.kyleu.projectile.models.result.filter.FilterSchema.reportFiltersArg
  implicit val filterType: ObjectType[GraphQLContext, Filter] = com.kyleu.projectile.models.result.filter.FilterSchema.filterType
  implicit val filterInputType: InputObjectType[Filter] = com.kyleu.projectile.models.result.filter.FilterSchema.filterInputType

  val orderBysArg = com.kyleu.projectile.models.result.orderBy.OrderBySchema.orderBysArg
  implicit val orderByType: ObjectType[GraphQLContext, OrderBy] = com.kyleu.projectile.models.result.orderBy.OrderBySchema.orderByType
  implicit val orderByInputType: InputObjectType[OrderBy] = com.kyleu.projectile.models.result.orderBy.OrderBySchema.orderByInputType
  implicit val pagingOptionsType: ObjectType[GraphQLContext, PagingOptions] = com.kyleu.projectile.models.result.paging.PagingSchema.pagingOptionsType

  implicit val tagType: ObjectType[GraphQLContext, Tag] = CommonSchema.tagType
  implicit val tagInputType: InputObjectType[Tag] = CommonSchema.tagInputType

  implicit val byteType: ScalarType[Byte] = CommonSchema.byteType
  implicit val shortType: ScalarType[Short] = CommonSchema.shortType
  implicit val floatType: ScalarType[Float] = CommonSchema.floatType
  implicit val uuidType: ScalarType[UUID] = CommonSchema.uuidType
  implicit val jsonType: ScalarType[Json] = CommonSchema.jsonType
  implicit val byteArrayType: ScalarType[Array[Byte]] = CommonSchema.byteArrayType

  implicit val zonedDateTimeType: ScalarType[ZonedDateTime] = DateTimeSchema.zonedDateTimeType
  implicit val localDateTimeType: ScalarType[LocalDateTime] = DateTimeSchema.localDateTimeType
  implicit val localDateType: ScalarType[LocalDate] = DateTimeSchema.localDateType
  implicit val localTimeType: ScalarType[LocalTime] = DateTimeSchema.localTimeType

  def deriveContextObjectType[Ctx, CtxVal, Val](
    fn: Ctx => CtxVal, config: DeriveObjectSetting[Ctx, Val]*
  ): ObjectType[Ctx, Val] = macro DeriveObjectTypeMacro.deriveContextObjectType[Ctx, CtxVal, Val]

  def deriveObjectType[Ctx, Val](config: DeriveObjectSetting[Ctx, Val]*): ObjectType[Ctx, Val] = macro DeriveObjectTypeMacro.deriveNormalObjectType[Ctx, Val]

  def deriveInputObjectType[T](config: DeriveInputObjectSetting*): InputObjectType[T] = macro DeriveInputObjectTypeMacro.deriveInputObjectType[T]

  def deriveEnumType[T](config: DeriveEnumSetting*): EnumType[T] = macro DeriveEnumTypeMacro.deriveEnumType[T]

  def deriveEnumeratumType[T <: enumeratum.EnumEntry](name: String, values: Seq[T]) = CommonSchema.deriveEnumeratumType(name, values)
  def deriveStringEnumeratumType[T <: enumeratum.values.StringEnumEntry](name: String, values: Seq[T]) = CommonSchema.deriveStringEnumeratumType(name, values)
}
