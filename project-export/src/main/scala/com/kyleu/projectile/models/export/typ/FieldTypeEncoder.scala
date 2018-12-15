package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.typ.FieldType.{EnumType, ListType, MapType, ObjectType, SetType, StructType}
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object FieldTypeEncoder {
  private[this] implicit val enumTypeEncoder: Encoder[EnumType] = deriveEncoder
  private[this] implicit val listTypeEncoder: Encoder[ListType] = deriveEncoder
  private[this] implicit val setTypeEncoder: Encoder[SetType] = deriveEncoder
  private[this] implicit val mapTypeEncoder: Encoder[MapType] = deriveEncoder
  private[this] implicit val structTypeEncoder: Encoder[StructType] = deriveEncoder
  private[this] implicit val objectTypeEncoder: Encoder[ObjectType] = deriveEncoder

  implicit val encodeFieldType: Encoder[FieldType] = (x: FieldType) => {
    val o = x match {
      case e: EnumType => e.asJson.asObject.get
      case l: ListType => l.asJson.asObject.get
      case s: SetType => s.asJson.asObject.get
      case m: MapType => m.asJson.asObject.get
      case s: StructType => s.asJson.asObject.get
      case o: ObjectType => o.asJson.asObject.get
      case _ => JsonObject.empty
    }
    o.add("t", x.value.asJson).asJson
  }
}
