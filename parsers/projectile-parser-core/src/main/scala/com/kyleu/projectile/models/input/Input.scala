package com.kyleu.projectile.models.input

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService, ExportUnion}
import com.kyleu.projectile.models.output.file.OutputFile

abstract class Input() extends Ordered[Input] {
  def template: InputTemplate
  def key: String
  def description: String

  def enums: Seq[ExportEnum] = Nil
  def enumOpt(k: String) = enums.find(_.key == k)
  def enum(k: String) = enumOpt(k).getOrElse {
    throw new IllegalStateException(s"No enum defined with key [$k] among candidates [${enums.map(_.key).sorted.mkString(", ")}]")
  }

  def models: Seq[ExportModel] = Nil
  def modelOpt(k: String) = models.find(_.key == k)
  def model(k: String) = modelOpt(k).getOrElse {
    throw new IllegalStateException(s"No model defined with key [$k] among candidates [${models.map(_.key).sorted.mkString(", ")}]")
  }

  def unions: Seq[ExportUnion] = Nil
  def unionOpt(k: String) = unions.find(_.key == k)
  def union(k: String) = unionOpt(k).getOrElse {
    throw new IllegalStateException(s"No union defined with key [$k] among candidates [${unions.map(_.key).sorted.mkString(", ")}]")
  }

  def services: Seq[ExportService] = Nil
  def serviceOpt(k: String) = services.find(_.key == k)
  def service(k: String) = serviceOpt(k).getOrElse {
    throw new IllegalStateException(s"No service defined with key [$k] among candidates [${services.map(_.key).sorted.mkString(", ")}]")
  }

  def additional: Seq[OutputFile] = Nil

  override def compare(that: Input) = key.compare(that.key)

  override def toString = s"$key - [${enums.size}] enums, [${models.size}] models, [${unions.size}] unions, and [${services.size}] services"

  lazy val hash = {
    val e = enums.map(_.hashCode).sum
    val m = models.map(_.hashCode).sum
    val s = services.map(_.hashCode).sum
    e + m + s
  }
}
