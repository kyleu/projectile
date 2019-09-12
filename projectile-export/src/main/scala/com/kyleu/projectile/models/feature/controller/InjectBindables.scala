package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic}
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectBindables extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ModelBindables.scala") {
  override def applies(config: ExportConfiguration) = config.enums.exists(_.features(EnumFeature.Controller))
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "models" :+ "module"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[(String, String)]], original: Seq[String]) = {
    val enums = config.enums.filter(_.features(EnumFeature.Controller))
    enumLogic(enums, config, original)
  }

  private[this] def enumLogic(enums: Seq[ExportEnum], config: ExportConfiguration, original: Seq[String]) = {
    val e = enums.sortBy(_.key)
    val newLines = e.flatMap { enum =>
      val sb = collection.mutable.ArrayBuffer.empty[String]

      if (!e.headOption.contains(enum)) {
        sb.append("")
      }
      val cp = enum.fullClassPath(config)
      val prop = ExportHelper.toIdentifier(config.project.key) + enum.className

      val (t, bind) = enum.inputType match {
        case InputType.Enum.ThriftIntEnum => "Int" -> "e.value.toString"
        case _ => "String" -> "e.value"
      }

      sb.append(s"private[this] def ${prop}Extractor(v: Either[String, $t]) = v match {")
      sb.append(s"  case Right(s) => Right($cp.withValue(s))")
      sb.append("  case Left(x) => throw new IllegalStateException(x)")
      sb.append("}")

      val pArg = s"implicit binder: play.api.mvc.PathBindable[$t]"
      sb.append(s"implicit def ${prop}PathBindable($pArg): play.api.mvc.PathBindable[$cp] = new play.api.mvc.PathBindable[$cp] {")
      sb.append(s"  override def bind(key: String, value: String) = ${prop}Extractor(binder.bind(key, value))")
      sb.append(s"  override def unbind(key: String, e: $cp) = $bind")
      sb.append("}")

      val qArg = s"implicit binder: play.api.mvc.QueryStringBindable[$t]"
      sb.append(s"implicit def ${prop}QueryStringBindable($qArg): play.api.mvc.QueryStringBindable[$cp] = new play.api.mvc.QueryStringBindable[$cp] {")
      sb.append(s"  override def bind(key: String, params: Map[String, Seq[String]]) = binder.bind(key, params).map(${prop}Extractor)")
      sb.append(s"  override def unbind(key: String, e: $cp) = $bind")
      sb.append("}")

      sb
    }
    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model bindables")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
