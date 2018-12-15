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
      intEnums = results.flatMap(_.allIntEnums),
      stringEnums = results.flatMap(_.allStringEnums),
      structs = results.flatMap(_.allStructs),
      services = results.flatMap(_.allServices)
    )
  }

  def loadFile(file: File) = parse(file)

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
