package com.projectile.services.graphql.original

import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile
import sangria.ast.FragmentDefinition
import sangria.schema.Schema

object GraphQLFragmentService {
  import GraphQLQueryParseService._

  def fragmentFile(cfg: GraphQLExportConfig, n: String, d: FragmentDefinition, nameMap: Map[String, ClassName], schema: Schema[_, _]) = {
    if (nameMap(n).provided) {
      None
    } else {
      val cn = nameMap(n)
      val file = ScalaFile(OutputPath.ServerSource, cn.pkg, cn.cn)
      file.addImport(cfg.providedPrefix ++ Seq("util", "JsonSerializers"), "_")

      meaningfulComments(d.comments).foreach(c => file.add("// " + c))

      GraphQLObjectHelper.objectFor(cfg, file, cn, schema.allTypes(d.typeCondition.name), d.selections, nameMap, schema, incEncoder = true)

      Some(file)
    }
  }
}
