package com.projectile.services.graphql.original

import com.projectile.models.graphql.GraphQLExportConfig
import com.projectile.models.output.file.ScalaFile
import com.projectile.models.output.{ExportHelper, OutputPath}
import com.projectile.services.graphql.original.GraphQLQueryParseService.ClassName

object GraphQLEnumService {
  private[this] val forbidden = Set("__directivelocation", "__typekind")

  def enumFile(cfg: GraphQLExportConfig, name: String, values: Seq[String], nameMap: Map[String, ClassName]) = {
    if (nameMap.get(name).exists(_.provided)) {
      None
    } else if (forbidden(name.toLowerCase)) {
      None
    } else {
      val cn = nameMap(name)
      val file = ScalaFile(OutputPath.ServerSource, cn.pkg, cn.cn)
      file.addImport(Seq("enumeratum", "values"), "_")

      file.add(s"sealed abstract class ${cn.cn}(override val value: String) extends StringEnumEntry")
      file.add()
      file.add(s"object ${cn.cn} extends StringEnum[${cn.cn}] with StringCirceEnum[${cn.cn}] {", 1)
      values.foreach(v => file.add(s"""case object ${ExportHelper.toClassName(v)} extends ${cn.cn}("$v")"""))
      file.add()
      file.add("override val values = findValues")
      file.add("}", -1)

      Some(file)
    }
  }
}
