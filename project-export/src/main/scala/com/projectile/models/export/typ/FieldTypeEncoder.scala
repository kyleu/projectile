package com.projectile.models.export.typ

import com.projectile.models.export.typ.FieldType.{EnumType, ListType, MapType, SetType, StructType}
import com.projectile.util.JsonSerializers._
import io.circe.JsonObject

object FieldTypeEncoder {
  private[this] implicit val enumTypeEncoder: Encoder[EnumType] = deriveEncoder
  private[this] implicit val listTypeEncoder: Encoder[ListType] = deriveEncoder
  private[this] implicit val setTypeEncoder: Encoder[SetType] = deriveEncoder
  private[this] implicit val mapTypeEncoder: Encoder[MapType] = deriveEncoder
  private[this] implicit val structTypeEncoder: Encoder[StructType] = deriveEncoder

  implicit val encodeFieldType: Encoder[FieldType] = (x: FieldType) => {
    val o = x match {
      case e: EnumType => e.asJson.asObject.get
      case l: ListType => l.asJson.asObject.get
      case s: SetType => s.asJson.asObject.get
      case m: MapType => m.asJson.asObject.get
      case s: StructType => s.asJson.asObject.get
      case _ => JsonObject.empty
    }
    o.add("t", x.value.asJson).asJson
  }
}
