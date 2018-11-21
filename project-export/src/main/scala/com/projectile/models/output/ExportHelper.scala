package com.projectile.models.output

import com.google.common.base.{CaseFormat, Converter}
import com.projectile.util.NumberUtils

object ExportHelper {
  private[this] val converters = collection.mutable.HashMap.empty[(CaseFormat, CaseFormat), Converter[String, String]]

  private[this] def getSource(s: String) = s match {
    case _ if s.contains('-') => CaseFormat.LOWER_HYPHEN
    case _ if s.contains('_') => if (s.headOption.exists(_.isUpper)) { CaseFormat.UPPER_UNDERSCORE } else { CaseFormat.LOWER_UNDERSCORE }
    case _ => if (s.headOption.exists(_.isUpper)) { CaseFormat.UPPER_CAMEL } else { CaseFormat.LOWER_CAMEL }
  }

  private[this] def converterFor(src: CaseFormat, tgt: CaseFormat) = converters.getOrElseUpdate(src -> tgt, src.converterTo(tgt))

  def toIdentifier(s: String) = converterFor(getSource(s), CaseFormat.LOWER_CAMEL).convert(s.replaceAllLiterally(" ", "").replaceAllLiterally(".", ""))
  def toClassName(s: String): String = if (s.nonEmpty && s == s.toUpperCase) {
    toClassName(s.toLowerCase)
  } else {
    converterFor(getSource(s), CaseFormat.UPPER_CAMEL).convert(s.replaceAllLiterally(" ", ""))
  }
  def toDefaultTitle(s: String) = toClassName(s).flatMap(c => if (c.isUpper) { Seq(' ', c) } else { Seq(c) }).trim

  val getAllArgs = "orderBy: Option[String] = None, limit: Option[Int] = None, offset: Option[Int] = None"
  val searchArgs = "q: Option[String], orderBy: Option[String] = None, limit: Option[Int] = None, offset: Option[Int] = None"

  def replaceBetween(filename: String, original: String, start: String, end: String, newContent: String) = {
    val oSize = NumberUtils.withCommas(original.length)
    val startIndex = original.indexOf(start)
    if (startIndex == -1) {
      throw new IllegalStateException(s"Cannot inject [$filename]. No start key matching [$start] in [$oSize] bytes.")
    }
    val endIndex = original.indexOf(end)
    if (endIndex == -1) {
      throw new IllegalStateException(s"Cannot inject [$filename]. No end key matching [$end] in [$oSize] bytes.")
    }

    original.substring(0, startIndex + start.length) + "\n" + newContent + "\n" + original.substring(endIndex)
  }
}
