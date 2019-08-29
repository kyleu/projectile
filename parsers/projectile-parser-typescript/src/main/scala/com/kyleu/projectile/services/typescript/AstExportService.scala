package com.kyleu.projectile.services.typescript

import better.files.File
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.JacksonUtils.printJackson
import com.kyleu.projectile.util.NumberUtils

import scala.sys.process._

object AstExportService {
  private[this] lazy val projDir = {
    val ret = new java.io.File("parsers/projectile-parser-typescript/src/main/typescript")
    if (!ret.exists()) {
      throw new IllegalStateException(s"Cannot load project directory [${ret.getPath}]")
    }
    ret
  }

  def compileTypeScript() = {
    val installResult = Process("npm install", projDir).!
    val compileResult = Process("tsc ast.ts", projDir).!
    s"Compiled - Install: $installResult, Compile: $compileResult"
  }

  def parseAst(root: File, cache: File, f: String, forceCompile: Boolean = false) = {
    val out = cache / f.replaceAllLiterally(".ts", ".json")
    if (out.isDirectory) { throw new IllegalStateException(s"Output file [${out.pathAsString}] is a directory") }
    val compilationResult = if (out.exists && !forceCompile) { None } else { Some(compile(root, f, out)) }
    compilationResult -> (parseJson(out.contentAsString) match {
      case Right(json) => json
      case Left(x) => throw x
    })
  }

  def compile(root: File, f: String, out: File) = {
    val startMs = System.currentTimeMillis
    val in = root / f
    if (!in.exists) {
      throw new IllegalStateException(s"Cannot load input file [${in.pathAsString}]")
    }

    out.createIfNotExists(createParents = true)
    val astCmd = s"node ${projDir.getPath}/ast.js ${in.pathAsString} ${out.pathAsString}"
    val astResult = Process(astCmd).!
    if (astResult != 0) {
      throw new IllegalStateException(s"Error running [$astCmd]")
    }

    if (!out.exists) { throw new IllegalStateException(s"Cannot load output file [${out.pathAsString}]") }
    s"Compiled [$f] in [${NumberUtils.withCommas(System.currentTimeMillis - startMs)}ms]"
  }

  private[this] def log(s: String) = println(s) // scalastyle:ignore

  def main(args: Array[String]): Unit = {
    import better.files.File
    args.toList match {
      case Nil => log(compileTypeScript())
      case in :: Nil =>
        val node = FileService.parseFile(root = File("."), cache = File("./.projectile/.cache/typescript"), path = in)
        log("Result: " + printJackson(node.asJson))
      case _ => throw new IllegalStateException("Zero or one argument only, please")
    }
  }
}
