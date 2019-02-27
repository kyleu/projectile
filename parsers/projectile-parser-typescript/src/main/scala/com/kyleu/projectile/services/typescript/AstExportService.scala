package com.kyleu.projectile.services.typescript

import com.kyleu.projectile.util.JsonSerializers._
import scala.sys.process._

object AstExportService {
  private[this] lazy val projDir = {
    val ret = new java.io.File("parsers/projectile-parser-typescript/src/main/resources")
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

  def parseAst(f: String) = {
    val file = better.files.File(f)
    if (!file.exists) {
      throw new IllegalStateException(s"Cannot load input file [${file.pathAsString}]")
    }

    val astCmd = s"node ast.js $f"
    val astResult = Process(astCmd, projDir).!
    if (astResult != 0) {
      throw new IllegalStateException(s"Error running [$astCmd]")
    }

    val out = better.files.File(f.replaceAllLiterally(".ts", ".json"))
    if (!out.exists) {
      throw new IllegalStateException(s"Cannot load output file [${out.pathAsString}]")
    }

    parseJson(out.contentAsString) match {
      case Right(json) => json
      case Left(x) => throw x
    }
  }

  def main(args: Array[String]): Unit = args.toList match {
    case Nil => log(compileTypeScript())
    case h :: Nil =>
      val json = parseAst(h)
      log("JSON Size: " + json.spaces2.length.toString)

      val node = TypeScriptNodeService.fromJson(json)
      log("Result: " + node.asJson.spaces2)
    case _ => throw new IllegalStateException("Zero or one argument only, please")
  }

  private[this] def log(s: String) = println(s)
}
