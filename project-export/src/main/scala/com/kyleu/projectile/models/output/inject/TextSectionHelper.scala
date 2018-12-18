package com.kyleu.projectile.models.output.inject

import com.kyleu.projectile.util.NumberUtils

object TextSectionHelper {
  case class Params(commentProvider: CommentProvider, key: String) {
    val start = commentProvider.comment(s"Start $key")
    val end = commentProvider.comment(s"End $key")
  }

  def replaceBetween(filename: String, original: Seq[String], p: Params, newLines: Seq[String], project: String) = {

    val (ws, prior, _, subsequent) = splitBetween(filename = filename, lines = original.toIndexedSeq, start = p.start, end = p.end)

    // val (before, currentSection, after) = splitBetween(filename = filename + ":section", original = current, start = p.sectionStart, end = p.sectionEnd)

    val finalContent = newLines.map(l => if (l.isEmpty) { "" } else { ws } + l)

    val trailingNewline = if (subsequent.lastOption.forall(_.isEmpty)) { Nil } else { Seq("") }
    prior ++ finalContent ++ subsequent ++ trailingNewline
  }

  private[this] def splitBetween(filename: String, lines: IndexedSeq[String], start: String, end: String) = {
    val oSize = NumberUtils.withCommas(lines.map(_.length + 1).sum)

    val startIndex = lines.indexWhere(_.contains(start))
    if (startIndex == -1) {
      throw new IllegalStateException(s"Cannot inject [$filename]. No start key matching [$start] in [$oSize] bytes.")
    }
    val endIndex = lines.indexWhere(_.contains(end))
    if (endIndex == -1) {
      throw new IllegalStateException(s"Cannot inject [$filename]. No end key matching [$end] in [$oSize] bytes.")
    }

    val wsSize = lines(startIndex).length - lines(startIndex).replaceAll("^\\s+", "").length
    val ws = (0 until wsSize).map(_ => " ").mkString
    val ret = (ws, lines.slice(0, startIndex + 1), lines.slice(startIndex + 1, endIndex), lines.slice(endIndex, lines.size))

    ret
  }
}
