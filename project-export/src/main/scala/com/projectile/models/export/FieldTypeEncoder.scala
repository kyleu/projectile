package com.projectile.models.export

import com.projectile.models.export.FieldType.ListType
import com.projectile.util.JsonSerializers._
import io.circe.JsonObject

object FieldTypeEncoder {
  private[this] implicit val listTypeEncoder: Encoder[ListType] = deriveEncoder

  val encodeFieldType: Encoder[FieldType] = (x: FieldType) => {
    val o = x match {
      case l: ListType => l.asJson.asObject.get
      case _ => JsonObject.empty
    }
    o.add("t", x.value.asJson).asJson
  }
}
