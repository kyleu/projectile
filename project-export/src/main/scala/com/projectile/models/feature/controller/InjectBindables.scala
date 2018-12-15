package com.projectile.models.feature.controller

import com.projectile.models.export.ExportEnum
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.feature.{EnumFeature, FeatureLogic}
import com.projectile.models.output.{ExportHelper, OutputPath}

object InjectBindables extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ModelBindables.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "util" :+ "web"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val enums = config.enums.filter(_.features(EnumFeature.Controller))
    if (enums.isEmpty) { original } else { enumLogic(enums, config, original) }
  }

  private[this] def enumLogic(enums: Seq[ExportEnum], config: ExportConfiguration, original: String) = {
    val startString = "  /* Begin model bindables */"
    val endString = "  /* End model bindables */"

    val newContent = enums.sortBy(_.key).map { enum =>
      val sb = new StringBuilder()

      sb.append(s"  import ${enum.fullClassPath(config)}\n")

      sb.append(s"  private[this] def ${enum.propertyName}Extractor(v: Either[String, String]) = v match {\n")
      sb.append(s"    case Right(s) => Right(${enum.className}.withValue(s))\n")
      sb.append(s"    case Left(x) => throw new IllegalStateException(x)\n")
      sb.append("  }\n")

      val pArg = "implicit stringBinder: PathBindable[String]"
      sb.append(s"  implicit def ${enum.propertyName}PathBindable($pArg): PathBindable[${enum.className}] = new PathBindable[${enum.className}] {\n")
      sb.append(s"    override def bind(key: String, value: String) = ${enum.propertyName}Extractor(stringBinder.bind(key, value))\n")
      sb.append(s"    override def unbind(key: String, x: ${enum.className}) = x.value\n")
      sb.append("  }\n")

      val qArg = "implicit stringBinder: QueryStringBindable[String]"
      val qTyp = s"QueryStringBindable[${enum.className}]"
      sb.append(s"  implicit def ${enum.propertyName}QueryStringBindable($qArg): $qTyp = new QueryStringBindable[${enum.className}] {\n")
      sb.append(s"    override def bind(key: String, params: Map[String, Seq[String]]) = stringBinder.bind(key, params).map(${enum.propertyName}Extractor)\n")
      sb.append(s"    override def unbind(key: String, x: ${enum.className}) = x.value\n")
      sb.append("  }\n")

      sb.toString
    }.mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = endString, newContent = newContent)
  }
}
