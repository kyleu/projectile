package com.kyleu.projectile.models.tag

import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.StringUtils

object Tag {
  implicit val jsonEncoder: Encoder[Tag] = deriveEncoder
  implicit val jsonDecoder: Decoder[Tag] = deriveDecoder

  def fromString(s: String) = StringUtils.toTags(s)

  def toString(seq: Seq[Tag]) = seq.map(t => t.k + "=" + t.v).mkString(",")

  def fromMap(m: Map[_, _]) = m.toList.map(e => Tag(e._1.toString, e._2.toString))
  def toMap(seq: Seq[Tag]) = seq.map(x => x.k -> x.v).toMap

  def fromJavaMap(m: java.util.HashMap[_, _]) = {
    import scala.collection.JavaConverters._
    fromMap(m.asScala.toMap)
  }

  def toJavaMap(seq: Seq[Tag]) = {
    val map = new java.util.HashMap[String, String]()
    seq.foreach(tag => map.put(tag.k, tag.v))
    map
  }
}

/** A simple key and value case class, used where unordered maps aren't appropriate */
case class Tag(k: String, v: String)
