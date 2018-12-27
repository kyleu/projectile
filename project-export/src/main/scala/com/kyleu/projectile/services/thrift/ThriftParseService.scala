package com.kyleu.projectile.services.thrift

import better.files._
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.kyleu.projectile.models.thrift.input.ThriftInput

object ThriftParseService {
  def loadThriftInput(files: Seq[File], t: ThriftInput) = {
    val results = files.map(loadFile)
    t.copy(
      typedefs = results.flatMap(_.allTypedefs).toMap,
      intEnums = clean(results.flatMap(_.allIntEnums).map(e => e.key -> e)),
      stringEnums = clean(results.flatMap(_.allStringEnums).map(e => e.key -> e)),
      structs = clean(results.flatMap(_.allStructs).map(e => e.key -> e)),
      services = clean(results.flatMap(_.allServices).map(e => e.key -> e))
    )
  }

  def loadFile(file: File) = parse(file)

  private[this] def clean[T](seq: Seq[(String, T)]) = {
    val grouped = seq.groupBy(_._1)
    grouped.values.map { group =>
      val ret = group.head
      val conflicts = group.tail.collect { case i if i._2.toString != ret._2.toString => i }
      val errs = conflicts.map { c => s" - ${c._2}" }
      if (errs.nonEmpty) {
        val msgs = s"Conflicts found for object [${ret._1}]" +: s" - ${ret._2}" +: errs
        throw new IllegalStateException(msgs.mkString("\n"))
      }
      ret
    }.toSeq.sortBy(_._1).map(_._2)
  }

  private[this] def parse(file: File): ThriftParseResult = {
    import scala.collection.JavaConverters._

    val src = Files.asByteSource(file.toJava).asCharSource(Charsets.UTF_8)
    val doc = ThriftIdlParser.parseThriftIdl(src)
    val h = doc.getHeader
    val d = doc.getDefinitions.asScala

    val pkg = Option(h.getNamespace("scala")).filterNot(_.isEmpty).orElse {
      Option(h.getNamespace("java")).filterNot(_.isEmpty)
    }.getOrElse(throw new IllegalStateException("No package"))

    val includes = h.getIncludes.asScala
    val included = includes.map { inc =>
      val f = file.parent / inc
      if (f.exists) {
        parse(f)
      } else {
        val other = file.parent / (inc + ".include")
        parse(other)
      }
    }
    ThriftParseResult(filename = file.name, pkg = pkg.split('.'), decls = d, includes = included, lines = file.lines.toSeq)
  }
}
