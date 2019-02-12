package com.kyleu.projectile.util

import com.kyleu.projectile.models.tag.Tag

import scala.io.Source

/** Exposes methods to split a string to an ordered sequence of lines, and produce a seq, tags, or map */
object StringUtils {
  def lines(s: String) = Source.fromString(s).getLines.toIndexedSeq

  def toList(s: String, delim: Char = ',') = s.split(delim).map(_.trim).filter(_.nonEmpty).toList

  def toTags(s: String, delim: Char = ',') = toList(s, delim).map(x => x.split('=').toList match {
    case h :: Nil => Tag(h, h)
    case h :: _ => Tag(h, x.stripPrefix(h).stripPrefix("=").trim)
    case _ => throw new IllegalStateException()
  })

  def toMap(s: String, delim: Char = ',') = toTags(s, delim).map(t => t.k -> t.v).toMap[String, String]

  def fromTags(s: Seq[Tag]) = s.map(t => t.k + "=" + t.v).mkString(",")
  def fromMap(m: Map[_, _]) = m.map(v => v._1 + "=" + v._2).mkString(",")
}
