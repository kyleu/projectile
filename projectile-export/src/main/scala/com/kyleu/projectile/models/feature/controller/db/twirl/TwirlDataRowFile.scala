package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlDataRowFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "DataRow")
    file.add(s"@(model: ${model.fullClassPath(config)}, additional: Option[Html] = None)<tr>", 1)

    model.searchFields.foreach { field =>
      val href = model.pkFields match {
        case Nil => ""
        case fields =>
          val args = fields.map(f => s"model.${f.propertyName}").mkString(", ")
          s"""@${TwirlHelper.routesClass(config, model)}.view($args)"""
      }
      file.add(s"<td>", 1)
      if (model.pkFields.exists(pkField => pkField.propertyName == field.propertyName)) {
        file.add(s"""<a href="$href">@model.${field.propertyName}</a>""")
      } else {
        field.t match {
          case FieldType.CodeType => file.add(s"<pre>@model.${field.propertyName}</pre>")
          case FieldType.BooleanType => file.add(s"@com.kyleu.projectile.views.html.components.form.booleanDisplay(model.${field.propertyName})")
          case _ => file.add(s"@model.${field.propertyName}")
        }
      }

      model.foreignKeys.find(_.references.forall(_.source == field.key)).foreach {
        case fk if config.getModelOpt(fk.targetTable).isDefined =>
          val tgt = config.getModel(fk.targetTable, s"foreign key ${fk.name}")
          val icon = TwirlHelper.iconHtml(config = config, propertyName = tgt.propertyName)
          if (!tgt.pkFields.forall(f => fk.references.map(_.target).contains(f.key))) {
            throw new IllegalStateException(s"FK [$fk] does not match PK [${tgt.pkFields.map(_.key).mkString(", ")}]...")
          }
          if (field.required) {
            file.add(s"""<a href="@${TwirlHelper.routesClass(config, tgt)}.view(model.${field.propertyName})">""", 1)
            file.add(icon)
            file.add("</a>", -1)
          } else {
            file.add(s"@model.${field.propertyName}.map { v =>", 1)
            file.add(s"""<a href="@${TwirlHelper.routesClass(config, tgt)}.view(v)">""", 1)
            file.add(icon)
            file.add("</a>", -1)
            file.add("}", -1)
          }
        case _ => // noop
      }

      file.add(s"</td>", -1)
    }
    file.add("@additional")
    file.add("</tr>", -1)
    file
  }
}
