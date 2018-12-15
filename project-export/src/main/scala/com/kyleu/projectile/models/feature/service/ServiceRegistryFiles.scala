package com.kyleu.projectile.models.feature.service

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.{ExportHelper, OutputPath}
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceRegistryFiles {
  def files(config: ExportConfiguration, models: Seq[ExportModel]) = {
    val packageModels = models.filter(_.inputType.isDatabase).filter(_.pkg.nonEmpty)
    val packages = packageModels.groupBy(_.pkg.head).toSeq.filter(_._2.nonEmpty).sortBy(_._1)

    val svcContent = packages.map(m => m._1 -> m._2.map { m =>
      s"""    val ${m.propertyName}Service: ${(config.applicationPackage ++ m.servicePackage :+ (m.className + "Service")).mkString(".")}"""
    }.sorted)

    svcContent.map { p =>
      val name = ExportHelper.toClassName(p._1) + "ServiceRegistry"
      val file = ScalaFile(path = OutputPath.ServerSource, dir = config.applicationPackage ++ Seq("services", p._1), key = name)
      file.add("@javax.inject.Singleton")
      file.add(s"class $name @javax.inject.Inject() (")
      file.add(p._2.mkString(",\n"))
      file.add(")")

      file
    }
  }
}
