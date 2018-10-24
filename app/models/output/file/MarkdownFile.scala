package models.output.file

import models.output.OutputPath

case class MarkdownFile(override val path: OutputPath, override val dir: Seq[String], override val key: String) extends OutputFile(
  path = path, dir = dir, key = key, filename = key + ".md"
) {
  override def prefix = "<!-- Generated File -->\n"

  def addHeader(s: String, level: Int = 1) = {
    add(s"${(0 until level).map(_ => "#").mkString} $s")
    add()
  }

  def addScala(lines: String*) = {
    add("```scala")
    lines.foreach(s => add(s))
    add("```")
  }

  override protected val icon = models.template.Icons.markdown
}
