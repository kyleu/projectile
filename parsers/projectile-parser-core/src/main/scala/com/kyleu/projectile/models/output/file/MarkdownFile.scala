package com.kyleu.projectile.models.output.file

import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.template.Icons

object MarkdownFile {
  val keyValHeaders = Seq(('l', 40, "Key"), ('l', 40, "Value"))

  def link(title: String, url: String = "") = s"[$title](${if (url.isEmpty) { title } else { url }})"

  def table(md: MarkdownFile, cols: Seq[(Char, Int, String)], content: Seq[Seq[String]]) = {
    md.add("| " + cols.map(c => " " + c._3.padTo(c._2 - 1, " ").mkString).mkString("|").trim)
    md.add("|" + cols.map(c => (c._1 match {
      case 'l' => (0 until c._2).map(_ => "-")
      case 'r' => (0 until (c._2 - 1)).map(_ => "-") :+ ':'
      case 'c' => ':' +: (0 until c._2 - 2).map(_ => "-") :+ ':'
      case _ => throw new IllegalStateException("Please pass one of [l, r, c] for alignment")
    }).mkString).mkString("|"))
    content.foreach { row =>
      md.add("| " + cols.zip(row).map(c => " " + c._2.padTo(c._1._2 - 1, " ").mkString).mkString("|").trim)
    }
  }

  def attributes(md: MarkdownFile, attrs: (String, String)*) = {
    md.add()
    md.add("## Attributes")
    table(md = md, cols = keyValHeaders, content = attrs.map(attr => Seq(attr._1, attr._2)))
  }
}

final case class MarkdownFile(override val path: OutputPath, override val dir: Seq[String], override val key: String) extends OutputFile(
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

  override protected val icon = Icons.markdown
}
