package com.kyleu.projectile.models.feature.graphql.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object SchemaMutationHelper {
  def addMutationFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.pkFields.nonEmpty) {
    val pkArgs = model.pkFields.map(pk => model.propertyName + pk.className + "Arg")
    val argProps = pkArgs.map(arg => s"c.arg($arg)").mkString(", ")

    file.add()
    file.add(s"val ${model.propertyName}MutationType = ObjectType(", 1)
    file.add(s"""name = "${model.className}Mutations",""")
    file.add("fields = fields(", 1)

    file.add(s"""unitField(name = "create", desc = None, t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.injectedService(config)}.create(c.ctx.creds, c.arg(dataFieldsArg))(td)""")
    file.add("}, dataFieldsArg),", -1)

    file.add(s"""unitField(name = "update", desc = None, t = OptionType(${model.propertyName}Type), f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.injectedService(config)}.update(c.ctx.creds, $argProps, c.arg(dataFieldsArg))(td).map(_._1)""")
    file.add(s"}, ${pkArgs.mkString(", ")}, dataFieldsArg),", -1)

    file.add(s"""unitField(name = "remove", desc = None, t = ${model.propertyName}Type, f = (c, td) => {""", 1)
    file.add(s"""c.ctx.${model.injectedService(config)}.remove(c.ctx.creds, $argProps)(td)""")
    file.add(s"}, ${pkArgs.mkString(", ")})", -1)

    file.add(")", -1)
    file.add(")", -1)

    file.add()
    val t = model.propertyName + "MutationType"
    val f = "(_, _) => scala.concurrent.Future.successful(())"
    file.add(s"""val mutationFields = fields(unitField(name = "${model.propertyName}", desc = None, t = $t, f = $f))""")
  }
}
