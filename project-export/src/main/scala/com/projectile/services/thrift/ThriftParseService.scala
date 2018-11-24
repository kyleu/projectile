package com.projectile.services.thrift

import com.projectile.models.thrift.schema.{ThriftEnum, ThriftService, ThriftStruct}

import better.files._
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files

import scala.concurrent.ExecutionContext

object ThriftParseService {
  def loadFiles(files: Seq[File]) = {
    val results = files.map(loadFile)
    (results.flatMap(_._1), results.flatMap(_._2), results.flatMap(_._3), results.flatMap(_._4))
  }

  def loadFile(file: File) = {
    val intEnums = Seq.empty[ThriftEnum]
    val stringEnums = Seq.empty[ThriftEnum]
    val structs = Seq.empty[ThriftStruct]
    val services = Seq.empty[ThriftService]

    (intEnums, stringEnums, structs, services)
  }
}
