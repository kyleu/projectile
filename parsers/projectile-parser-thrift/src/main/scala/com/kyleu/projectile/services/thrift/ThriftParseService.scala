package com.kyleu.projectile.services.thrift

import better.files._
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.kyleu.projectile.models.thrift.input.ThriftInput

object ThriftParseService {
  def loadThriftInput(files: Seq[(Boolean, File)], t: ThriftInput) = {
    val results = files.map(f => loadFile(f._1, f._2))
    t.copy(
      typedefs = results.flatMap(_.allTypedefs).toMap,
      intEnums = clean(results.flatMap(_.allIntEnums).map(e => e.key -> e)),
      stringEnums = clean(results.flatMap(_.allStringEnums).map(e => e.key -> e)),
      structs = clean(results.flatMap(_.allStructs).map(e => e.key -> e)),
      thriftUnions = clean(results.flatMap(_.allUnions).map(e => e.key -> e)),
      thriftServices = clean(results.flatMap(_.allServices).map(e => e.key -> e))
    )
  }

  def loadFile(reference: Boolean, file: File) = parse(reference, file)

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

  private[this] def parse(reference: Boolean, file: File): ThriftParseResult = {
    import scala.jdk.CollectionConverters._

    val src = Files.asByteSource(file.toJava).asCharSource(Charsets.UTF_8)
    val doc = ThriftIdlParser.parseThriftIdl(src)
    val h = doc.getHeader
    val d = doc.getDefinitions.asScala

    val pkg = Option(h.getNamespace("scala")).filterNot(_.isEmpty).orElse {
      Option(h.getNamespace("java")).filterNot(_.isEmpty)
    }.getOrElse(throw new IllegalStateException("No package"))

    val included = h.getIncludes.asScala.map { inc =>
      val f = file.parent / inc
      if (f.exists) {
        parse(reference = true, file = f)
      } else {
        val other = file.parent / (inc + ".include")
        parse(reference = true, file = other)
      }
    }
    ThriftParseResult(
      filename = file.name,
      reference = reference,
      pkg = pkg.split('.').toIndexedSeq,
      decls = d.toIndexedSeq,
      includes = included.toIndexedSeq,
      lines = file.lines.toIndexedSeq
    )
  }
}
