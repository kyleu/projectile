package com.kyleu.projectile.models.output.inject

import com.kyleu.projectile.util.NumberUtils

object TextSectionHelper {
  private[this] val sectionKey = "Projectile export section ["

  case class Params(commentProvider: CommentProvider, key: String) {
    val start = commentProvider.comment(s"Start $key")
    val end = commentProvider.comment(s"End $key")
  }

  def replaceBetween(filename: String, original: Seq[String], p: Params, newLines: Seq[String], project: String) = {
    val (ws, prior, current, subsequent) = splitBetween(filename = filename, lines = original.toIndexedSeq, start = p.start, end = p.end)

    val finalContent = {
      val currentSections = getSections(p, current)
      val newSection = newLines.map(l => if (l.isEmpty) { "" } else { ws } + l)
      val finalSections = withSection(ws, p, currentSections, project, newSection)
      finalSections.flatMap(_._2)
    }

    prior ++ finalContent ++ subsequent
  }

  private[this] def splitBetween(filename: String, lines: IndexedSeq[String], start: String, end: String) = {
    val oSize = NumberUtils.withCommas(lines.map(_.length + 1).sum)

    val startIndex = lines.indexWhere(_.contains(start))
    if (startIndex == -1) {
      throw new IllegalStateException(s"Cannot inject [$filename]. No start key matching [$start] in [$oSize] bytes")
    }
    val endIndex = lines.indexWhere(_.contains(end))
    if (endIndex == -1) {
      throw new IllegalStateException(s"Cannot inject [$filename]. No end key matching [$end] in [$oSize] bytes")
    }

    val wsSize = lines(startIndex).length - lines(startIndex).replaceAll("^\\s+", "").length
    val ws = (0 until wsSize).map(_ => " ").mkString
    val ret = (ws, lines.slice(0, startIndex + 1), lines.slice(startIndex + 1, endIndex), lines.slice(endIndex, lines.size))

    ret
  }

  private[this] def getSections(p: Params, lines: IndexedSeq[String]) = {
    lines.foldLeft(List.empty[(String, Seq[String])]) { (l, r) =>
      val prior = if (l.isEmpty) { Nil } else { l.init }
      val current = l match {
        case Nil => "_prelude" -> Nil
        case _ => l.lastOption.getOrElse(throw new IllegalStateException())
      }

      r.indexOf(sectionKey) match {
        case -1 => prior :+ ((current._1, current._2 :+ r))
        case idx =>
          val sectionName = r.substring(idx + sectionKey.length, r.indexOf("]", idx))
          prior :+ current :+ ((sectionName, Seq(r)))
      }
    }
  }

  private[this] def withSection(ws: String, p: Params, sections: List[(String, Seq[String])], key: String, content: Seq[String]) = {
    if (content.exists(_.trim.nonEmpty)) {
      val section = {
        val comment = p.commentProvider.comment(s"$sectionKey$key]")
        (key, (ws + comment) +: content)
      }
      val fixed = sections.map {
        case s if s._1 == key => section
        case s => s
      }
      if (fixed.exists(_._1 == key)) {
        fixed
      } else {
        (fixed :+ section).sortBy(_._1)
      }
    } else {
      sections
    }
  }
}
