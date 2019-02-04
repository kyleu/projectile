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
      case e: EnumType => e.asJson.asObject.getOrElse(throw new IllegalStateException())
      case l: ListType => l.asJson.asObject.getOrElse(throw new IllegalStateException())
      case s: SetType => s.asJson.asObject.getOrElse(throw new IllegalStateException())
      case m: MapType => m.asJson.asObject.getOrElse(throw new IllegalStateException())
      case s: StructType => s.asJson.asObject.getOrElse(throw new IllegalStateException())
      case o: ObjectType => o.asJson.asObject.getOrElse(throw new IllegalStateException())
      case _ => JsonObject.empty
    }
    o.add("t", x.value.asJson).asJson
  }
}
