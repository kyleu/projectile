package com.projectile.services.thrift

import better.files._
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files

object ThriftParseService {
  def loadFiles(files: Seq[File]) = files.map(loadFile)

  def loadFile(file: File) = {
    parse(file)
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
    ThriftParseResult(
      filename = file.name,
      srcPkg = pkg.split('.'),
      decls = d,
      includes = included,
      lines = file.lines.toSeq
    )
  }
}
