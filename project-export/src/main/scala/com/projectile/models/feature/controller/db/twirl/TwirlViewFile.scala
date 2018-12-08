package com.projectile.models.feature.controller.db.twirl

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.file.TwirlFile

object TwirlViewFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val args = model.pkFields.map(field => s"model.${field.propertyName}").mkString(", ")
    val file = TwirlFile(config.applicationPackage ++ model.viewPackage, model.propertyName + "View")
    val modelPath = (config.applicationPackage :+ "models").mkString(".")
    val audits = if (model.features(ModelFeature.Audit)) { s", auditRecords: Seq[$modelPath.audit.AuditRecord]" } else { "" }
    file.add(s"@(user: $modelPath.user.SystemUser, model: ${model.fullClassPath(config)}, notes: Seq[$modelPath.note.Note]$audits, debug: Boolean)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: ${config.utilitiesPackage.mkString(".")}.tracing.TraceData")
    val toInterp = model.pkFields.map(c => "${model." + c.propertyName + "}").mkString(", ")
    val viewPkg = (config.viewPackage ++ Seq("html", "admin")).mkString(".")
    file.add(s""")@traceData.logClass(getClass)@$viewPkg.layout.page(user, "explore", s"${model.title} [$toInterp]") {""", 1)

    file.add("""<div class="collection with-header">""", 1)
    file.add("<div class=\"collection-header\">", 1)
    if (model.pkFields.nonEmpty) {
      val onClick = s"""onclick="return confirm('Are you sure you want to remove this ${model.title}?')""""
      file.add(s"""<div class="right"><a class="theme-text" href="@${TwirlHelper.routesClass(config, model)}.editForm($args)">Edit</a></div>""")
      file.add(s"""<div class="right"><a class="theme-text remove-link" $onClick href="@${TwirlHelper.routesClass(config, model)}.remove($args)">Remove</a></div>""")
    }
    file.add("<h5>", 1)
    file.add(s"""<a class="theme-text" href="@${TwirlHelper.routesClass(config, model)}.list()">""" + TwirlHelper.iconHtml(config, model.propertyName) + "</a>")
    val toTwirl = model.pkFields.map(c => "@model." + c.propertyName).mkString(", ")
    file.add(s"""${model.title} [$toTwirl]""")
    file.add("</h5>", -1)
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
    file.add("<table class=\"highlight\">", 1)
    file.add("<tbody>", 1)
    model.fields.foreach { field =>
      file.add("<tr>", 1)
      file.add(s"<th>${field.title}</th>")
      model.foreignKeys.find(_.references.forall(_.source == field.key)) match {
        case Some(fk) if config.getModelOpt(fk.targetTable).isDefined =>
          file.add("<td>", 1)
          val tgt = config.getModel(fk.targetTable)
          if (!tgt.pkFields.forall(f => fk.references.map(_.target).contains(f.key))) {
            throw new IllegalStateException(s"FK [$fk] does not match PK [${tgt.pkFields.map(_.key).mkString(", ")}]...")
          }
          if (field.required) {
            file.add(s"@model.${field.propertyName}")
          } else {
            file.add(s"@model.${field.propertyName}.getOrElse(${config.utilitiesPackage.mkString(".")}.NullUtils.str)")
          }
          if (field.required) {
            file.add(s"""<a class="theme-text" href="@${TwirlHelper.routesClass(config, tgt)}.view(model.${field.propertyName})">${TwirlHelper.iconHtml(config, tgt.propertyName)}</a>""")
          } else {
            file.add(s"@model.${field.propertyName}.map { v =>", 1)
            file.add(s"""<a class="theme-text" href="@${TwirlHelper.routesClass(config, tgt)}.view(v)">${TwirlHelper.iconHtml(config, tgt.propertyName)}</a>""")
            file.add("}", -1)
          }
          file.add("</td>", -1)
        case _ if field.t == FieldType.CodeType || field.t == FieldType.JsonType => file.add(s"<td><pre>@model.${field.propertyName}<pre></td>")
        case _ => file.add(s"<td>@model.${field.propertyName}</td>")
      }
      file.add("</tr>", -1)
    }
    file.add("</tbody>", -1)
    file.add("</table>", -1)
    file.add("</div>", -1)

    if (model.pkFields.nonEmpty) {
      val modelPks = model.pkFields.map(f => s"model.${f.propertyName}").mkString(", ")
      file.add(s"""@$viewPkg.note.notes(notes, "${model.propertyName}", "${model.title}", $modelPks)""")
      if (model.features(ModelFeature.Audit)) {
        file.add(s"""@$viewPkg.audit.auditRecords(auditRecords, "${model.propertyName}", "${model.title}", $modelPks)""")
      }
    }
    file.add("</div>", -1)
    addReferences(config, model, file)
    file.add("}", -1)

    file
  }

  def addReferences(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.validReferences(config).nonEmpty) {
    val args = model.pkFields.map(field => s"model.${field.propertyName}").mkString(", ")
    file.add()
    file.add("""<ul id="model-relations" class="collapsible" data-collapsible="expandable">""", 1)
    model.transformedReferences(config).foreach { r =>
      val src = r._3
      val srcField = r._4
      val tgtField = r._2
      val relArgs = s"""data-table="${src.propertyName}" data-field="${srcField.propertyName}" data-singular="${src.title}" data-plural="${src.plural}""""
      val relAttrs = s"""id="relation-${src.propertyName}-${srcField.propertyName}" $relArgs"""
      val relUrl = TwirlHelper.routesClass(config, src) + s".by${srcField.className}(model.${tgtField.propertyName}, limit = Some(5))"
      file.add(s"""<li $relAttrs data-url="@$relUrl">""", 1)
      file.add("""<div class="collapsible-header">""", 1)
      file.add(TwirlHelper.iconHtml(config, src.propertyName))
      file.add(s"""<span class="title">${src.plural}</span>&nbsp;by ${srcField.title}""")
      file.add("</div>", -1)
      file.add(s"""<div class="collapsible-body"><span>Loading...</span></div>""")
      file.add("</li>", -1)
    }
    file.add("</ul>", -1)
    file.add(s"@${(config.viewPackage ++ Seq("html", "components")).mkString(".")}.includeScalaJs(debug)")
    file.add(s"""<script>$$(function() { new RelationService('@${TwirlHelper.routesClass(config, model)}.relationCounts($args)') });</script>""")
  }
}
