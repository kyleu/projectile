package models.output.feature.controller

import models.export.config.ExportConfiguration
import models.output.feature.{EnumFeature, FeatureLogic}
import models.output.{ExportHelper, OutputPath}

object InjectBindables extends FeatureLogic.Inject(path = OutputPath.ServerSource, filename = "ModelBindables.scala") {
  override def dir(config: ExportConfiguration) = config.applicationPackage :+ "util" :+ "web"

  override def logic(config: ExportConfiguration, markers: Map[String, Seq[String]], original: String) = {
    val startString = "  /* Begin model bindables */"
    val newContent = config.enums.filter(_.features(EnumFeature.Controller)).sortBy(_.key).map { enum =>
      val sb = new StringBuilder()

      sb.append(s"  import ${enum.fullClassPath(config)}\n")

      sb.append(s"  private[this] def ${enum.propertyName}Extractor(v: Either[String, String]) = v match {\n")
      sb.append(s"    case Right(s) => Right(${enum.className}.withValue(s))\n")
      sb.append(s"    case Left(x) => throw new IllegalStateException(x)\n")
      sb.append("  }\n")

      sb.append(s"  implicit def ${enum.propertyName}PathBindable(implicit stringBinder: PathBindable[String]): PathBindable[${enum.className}] = new PathBindable[${enum.className}] {\n")
      sb.append(s"    override def bind(key: String, value: String) = ${enum.propertyName}Extractor(stringBinder.bind(key, value))\n")
      sb.append(s"    override def unbind(key: String, x: ${enum.className}) = x.value\n")
      sb.append("  }\n")

      sb.append(s"  implicit def ${enum.propertyName}QueryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[${enum.className}] = new QueryStringBindable[${enum.className}] {\n")
      sb.append(s"    override def bind(key: String, params: Map[String, Seq[String]]) = stringBinder.bind(key, params).map(${enum.propertyName}Extractor)\n")
      sb.append(s"    override def unbind(key: String, x: ${enum.className}) = x.value\n")
      sb.append("  }\n")

      sb.toString
    }.mkString("\n")
    ExportHelper.replaceBetween(filename = filename, original = original, start = startString, end = "  /* End model bindables */", newContent = newContent)
  }
}
