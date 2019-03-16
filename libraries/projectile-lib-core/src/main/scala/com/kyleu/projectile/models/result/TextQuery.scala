package com.kyleu.projectile.models.result

import com.kyleu.projectile.util.JsonSerializers._

class TextQuery[T: Decoder](val name: String) {
  def getData(json: Json): Option[T] = if (json.isNull) { None } else { Some(extract[T](json)) }
  def content = ""
}
