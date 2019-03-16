package com.kyleu.projectile.models.export.typ

import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object FieldTypeEncoder {
  private[this] implicit val enumTypeEncoder: Encoder[EnumType] = deriveEncoder
  private[this] implicit val structTypeEncoder: Encoder[StructType] = deriveEncoder
  private[this] implicit val objectTypeEncoder: Encoder[ObjectType] = deriveEncoder
  private[this] implicit val unionTypeEncoder: Encoder[UnionType] = deriveEncoder

  private[this] implicit val methodTypeEncoder: Encoder[MethodType] = deriveEncoder

  private[this] implicit val listTypeEncoder: Encoder[ListType] = deriveEncoder
  private[this] implicit val setTypeEncoder: Encoder[SetType] = deriveEncoder
  private[this] implicit val mapTypeEncoder: Encoder[MapType] = deriveEncoder

  private[this] implicit val exoticTypeEncoder: Encoder[ExoticType] = deriveEncoder

  implicit val encodeFieldType: Encoder[FieldType] = (x: FieldType) => {
    val o = x match {
      case e: EnumType => e.asJson.asObject.getOrElse(throw new IllegalStateException("Cannot encode enum"))
      case s: StructType => s.asJson.asObject.getOrElse(throw new IllegalStateException("Cannot encode struct"))
      case o: ObjectType => o.asJson.asObject.getOrElse(throw new IllegalStateException("Cannot encode object"))
      case u: UnionType => u.asJson.asObject.getOrElse(throw new IllegalStateException("Cannot encode union"))

      case m: MethodType => m.asJson.asObject.getOrElse(throw new IllegalStateException(s"Cannot encode method(${m.params.mkString(". ")}): ${m.ret}"))

      case l: ListType => l.asJson.asObject.getOrElse(throw new IllegalStateException(s"Cannot encode list[${l.typ}]"))
      case s: SetType => s.asJson.asObject.getOrElse(throw new IllegalStateException(s"Cannot encode set[${s.typ}]"))
      case m: MapType => m.asJson.asObject.getOrElse(throw new IllegalStateException(s"Cannot encode map[${m.k}, ${m.v}]"))

      case x: ExoticType => x.asJson.asObject.getOrElse(throw new IllegalStateException(s"Cannot encode exotic[${x.key}]"))

      case _ => JsonObject.empty
    }
    o.add("t", x.value.asJson).asJson
  }
}
