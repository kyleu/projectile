package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.{EnumFeature, FeatureLogic}
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.inject.{CommentProvider, TextSectionHelper}

object InjectBindables extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ModelBindables.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "util" :+ "web"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: Seq[String]) = {
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
      sb.append(s"import ${enum.fullClassPath(config)}")

      sb.append(s"private[this] def ${enum.propertyName}Extractor(v: Either[String, String]) = v match {")
      sb.append(s"  case Right(s) => Right(${enum.className}.withValue(s))")
      sb.append(s"  case Left(x) => throw new IllegalStateException(x)")
      sb.append("}")

      val pArg = "implicit stringBinder: PathBindable[String]"
      sb.append(s"implicit def ${enum.propertyName}PathBindable($pArg): PathBindable[${enum.className}] = new PathBindable[${enum.className}] {")
      sb.append(s"  override def bind(key: String, value: String) = ${enum.propertyName}Extractor(stringBinder.bind(key, value))")
      sb.append(s"  override def unbind(key: String, x: ${enum.className}) = x.value")
      sb.append("}")

      val qArg = "implicit stringBinder: QueryStringBindable[String]"
      val qTyp = s"QueryStringBindable[${enum.className}]"
      sb.append(s"implicit def ${enum.propertyName}QueryStringBindable($qArg): $qTyp = new QueryStringBindable[${enum.className}] {")
      sb.append(s"  override def bind(key: String, params: Map[String, Seq[String]]) = stringBinder.bind(key, params).map(${enum.propertyName}Extractor)")
      sb.append(s"  override def unbind(key: String, x: ${enum.className}) = x.value")
      sb.append("}")

      sb
    }
    val params = TextSectionHelper.Params(commentProvider = CommentProvider.Scala, key = "model bindables")
    TextSectionHelper.replaceBetween(filename = filename, original = original, p = params, newLines = newLines, project = config.project.key)
  }
}
