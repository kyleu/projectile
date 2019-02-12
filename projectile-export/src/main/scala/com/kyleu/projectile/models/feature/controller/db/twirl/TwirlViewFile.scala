package com.kyleu.projectile.models.feature.controller.db.twirl

import com.kyleu.projectile.models.export.{ExportField, ExportModel}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.TwirlFile

object TwirlViewFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val args = model.pkFields.map(field => s"model.${field.propertyName}").mkString(", ")
    val file = TwirlFile(model.viewPackage(config), model.propertyName + "View")
    val modelPath = (config.applicationPackage :+ "models").mkString(".")

    val su = CommonImportHelper.getString(config, "SystemUser")
    val nu = CommonImportHelper.getString(config, "NullUtils")
    val aa = CommonImportHelper.getString(config, "AuthActions")
    val audits = if (model.features(ModelFeature.Audit)) { s", auditRecords: Seq[$modelPath.audit.AuditRecord]" } else { "" }
    val notes = if (model.features(ModelFeature.Notes)) { s", notes: Seq[${CommonImportHelper.getString(config, "Note")}]" } else { "" }

    file.add(s"@(user: $su, authActions: $aa, model: ${model.fullClassPath(config)}$notes$audits, debug: Boolean)(")
    val td = s"${(config.utilitiesPackage :+ "tracing").mkString(".")}.TraceData"
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: $td")
    val toInterp = model.pkFields.map(c => "${model." + c.propertyName + "}").mkString(", ")
    val viewPkg = (config.viewPackage ++ Seq("html", "admin")).mkString(".")
    val systemViewPkg = (config.systemViewPackage ++ Seq("html", "admin")).mkString(".")
    file.add(s""")@$systemViewPkg.layout.page(user, authActions, "explore", s"${model.title} [$toInterp]") {""", 1)

    file.add("""<div class="collection with-header">""", 1)
    file.add("<div class=\"collection-header\">", 1)
    if (model.pkFields.nonEmpty) {
      val onClick = s"""onclick="return confirm('Are you sure you want to remove this ${model.title}?')""""
      file.add(s"""<div class="right"><a class="theme-text" href="@${TwirlHelper.routesClass(config, model)}.editForm($args)">Edit</a></div>""")
      val rc = TwirlHelper.routesClass(config, model)
      file.add(s"""<div class="right"><a class="theme-text remove-link" $onClick href="@$rc.remove($args)">Remove</a></div>""")
    }
    file.add("<h5>", 1)
    val modelIcon = TwirlHelper.iconHtml(config, model.propertyName)
    file.add(s"""<a class="theme-text" href="@${TwirlHelper.routesClass(config, model)}.list()">""" + modelIcon + "</a>")
    val toTwirl = model.pkFields.map(c => "@model." + c.propertyName).mkString(", ")
    file.add(s"""${model.title} [$toTwirl]""")
    file.add("</h5>", -1)
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
    file.add("<table class=\"highlight\">", 1)
    file.add("<tbody>", 1)
    addFields(config, model, file)
    file.add("</tbody>", -1)
    file.add("</table>", -1)
    file.add("</div>", -1)

    if (model.pkFields.nonEmpty) {
      val modelPks = model.pkFields.map(f => s"model.${f.propertyName}").mkString(", ")
      if (model.features(ModelFeature.Notes)) {
        file.add(s"""@$viewPkg.note.notes(notes, "${model.propertyName}", "${model.title}", $modelPks)""")
      }
      if (model.features(ModelFeature.Audit)) {
        file.add(s"""@$viewPkg.audit.auditRecords(auditRecords, "${model.propertyName}", "${model.title}", $modelPks)""")
      }
    }
    file.add("</div>", -1)
    addReferences(config, model, file)
    file.add("}", -1)

    file
  }

  private[this] def addReferences(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.validReferences(config).nonEmpty) {
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
      file.add("""<div class="collapsible-body"><span>Loading...</span></div>""")
      file.add("</li>", -1)
    }
    file.add("</ul>", -1)
    file.add(s"@${(config.viewPackage ++ Seq("html", "components")).mkString(".")}.includeScalaJs(debug)")
    file.add(s"""<script>$$(function() { new RelationService('@${TwirlHelper.routesClass(config, model)}.relationCounts($args)') });</script>""")
  }

  private[this] def forField(config: ExportConfiguration, field: ExportField) = field.t match {
    case FieldType.ListType(_) if field.required => s"""@model.${field.propertyName}.mkString(", ")"""
    case FieldType.ListType(_) => s"""@model.${field.propertyName}.map(_.mkString(", "))"""
    case FieldType.SetType(_) if field.required => s"""@model.${field.propertyName}.mkString(", ")"""
    case FieldType.SetType(_) => s"""@model.${field.propertyName}.map(_.mkString(", "))"""
    case FieldType.MapType(_, _) if field.required => s"""@model.${field.propertyName}.map(x => x._2 + "=" + x._1).mkString(", ")"""
    case FieldType.MapType(_, _) => s"""@model.${field.propertyName}.map(_.map(x => x._2 + "=" + x._1).mkString(", "))"""
    case FieldType.TagsType if field.required => s"""@model.${field.propertyName}.map(x => x.k + "=" + x.v).mkString(", ")"""
    case FieldType.TagsType => s"""@model.${field.propertyName}.map(_.map(x => x.k + "=" + x.v).mkString(", "))"""

    case FieldType.CodeType | FieldType.JsonType if field.required => s"<pre>@model.${field.propertyName}<pre>"
    case FieldType.CodeType | FieldType.JsonType => s"<pre>@model.${field.propertyName}.getOrElse(com.kyleu.projectile.util.NullUtils.str)<pre>"

    case _ if field.required => s"@model.${field.propertyName}"
    case _ =>
      val nu = CommonImportHelper.getString(config, "NullUtils")
      s"@model.${field.propertyName}.getOrElse($nu.str)"
  }

  private[this] def addFields(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = model.fields.foreach { field =>
    file.add("<tr>", 1)
    file.add(s"<th>${field.title}</th>")
    model.foreignKeys.find(_.references.forall(_.source == field.key)) match {
      case Some(fk) if config.getModelOpt(fk.targetTable).isDefined =>
        file.add("<td>", 1)
        val tgt = config.getModel(fk.targetTable, s"foreign key ${fk.name}")
        if (!tgt.pkFields.forall(f => fk.references.map(_.target).contains(f.key))) {
          throw new IllegalStateException(s"FK [$fk] does not match PK [${tgt.pkFields.map(_.key).mkString(", ")}]...")
        }
        file.add(forField(config, field))
        if (field.required) {
          val icon = TwirlHelper.iconHtml(config, tgt.propertyName)
          file.add(s"""<a class="theme-text" href="@${TwirlHelper.routesClass(config, tgt)}.view(model.${field.propertyName})">$icon</a>""")
        } else {
          file.add(s"@model.${field.propertyName}.map { v =>", 1)
          val rc = TwirlHelper.routesClass(config, tgt)
          file.add(s"""<a class="theme-text" href="@$rc.view(v)">${TwirlHelper.iconHtml(config, tgt.propertyName)}</a>""")
          file.add("}", -1)
        }
        file.add("</td>", -1)
      case _ => file.add(s"<td>${forField(config, field)}</td>")
    }
    file.add("</tr>", -1)
  }
}
