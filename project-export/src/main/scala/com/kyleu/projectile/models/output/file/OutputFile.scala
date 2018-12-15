package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.util.JsonSerializers._

object OutputFile {
  object Rendered {
    implicit val jsonEncoder: Encoder[Rendered] = deriveEncoder
    implicit val jsonDecoder: Decoder[Rendered] = deriveDecoder
  }

  case class Rendered(path: OutputPath, dir: Seq[String], key: String, filename: String, content: String, icon: String, markers: Map[String, Seq[String]]) {
    val filePath = s"${dir.map(_ + "/").mkString}$filename"
    override val toString = s"$path:$filePath"
  }
}

abstract class OutputFile(val path: OutputPath, val dir: Seq[String], val key: String, val filename: String) {
  private[this] var hasRendered = false
  private[this] var currentIndent = 0
  private[this] val lines = collection.mutable.ArrayBuffer.empty[String]

  protected def icon: String

  private[this] val markers = collection.mutable.HashMap.empty[String, Seq[String]]
  def markersFor(key: String) = markers.getOrElseUpdate(key, Nil)
  def addMarker(key: String, v: String) = markers(key) = markersFor(key) :+ v

  def indent(indentDelta: Int = 1): Unit = currentIndent += indentDelta

  def add(line: String = "", indentDelta: Int = 0): Unit = {
    if (hasRendered) {
      throw new IllegalStateException("Already rendered.")
    }
    if (indentDelta < 0) {
      currentIndent += indentDelta
    }
    val ws = if (line.trim.isEmpty) { "" } else { (0 until currentIndent).map(_ => "  ").mkString }
    if (indentDelta > 0) {
      currentIndent += indentDelta
    }

    lines += (ws + line + "\n")
  }

  def prefix: String = ""
  def suffix: String = ""

  lazy val rendered = {
    hasRendered = true
    val content = prefix + lines.mkString + suffix
    OutputFile.Rendered(path = path, dir = dir, key = key, filename = filename, content = content, icon = icon, markers = markers.toMap)
  }
}
