package models.output.file

import models.output.OutputPath

case class ScalaFile(
    override val path: OutputPath, override val dir: Seq[String], override val key: String
) extends OutputFile(path = path, dir = dir, key = key, filename = key + ".scala") {

  private[this] var imports = Set.empty[(String, String)]

  def addImport(p: String, c: String) = {
    imports += (p -> c)
  }

  override def prefix = {
    val pkgString = if (dir.isEmpty) { "" } else { s"\npackage ${dir.mkString(".")}\n" }

    val importString = if (imports.isEmpty) {
      ""
    } else {
      imports.toSeq.groupBy(_._1).mapValues(_.map(_._2)).toList.sortBy(_._1).map { i =>
        i._2.size match {
          case 1 => s"import ${i._1}.${i._2.head}"
          case _ => s"import ${i._1}.{${i._2.sorted.mkString(", ")}}"
        }
      }.mkString("\n") + "\n\n"
    }

    s"/* Generated File */$pkgString\n$importString"
  }

  override protected val icon = models.template.Icons.scala
}
