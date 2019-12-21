package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.util.JsonSerializers._

object ExportModelReference {
  final case class Ref(r: ExportModelReference, f: ExportField, src: ExportModel, tf: ExportField)

  object Ref {
    implicit val jsonEncoder: Encoder[Ref] = deriveEncoder
    implicit val jsonDecoder: Decoder[Ref] = deriveDecoder
  }

  implicit val jsonEncoder: Encoder[ExportModelReference] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportModelReference] = deriveDecoder

  def validReferences(config: ExportConfiguration, model: ExportModel) = {
    val refs = model.references.filter(ref => config.getModelOpt(ref.srcTable).exists(_.features(ModelFeature.Core)))
    refs.groupBy(x => (x.srcCol, x.srcTable, x.tgt)).map(_._2.headOption.getOrElse(throw new IllegalStateException())).toList
  }

  def transformedReferences(config: ExportConfiguration, model: ExportModel) = validReferences(config, model).flatMap { r =>
    val src = config.getModel(r.srcTable, s"${model.className} reference ${r.name}")
    model.getFieldOpt(r.tgt).toList.flatMap(f => src.getFieldOpt(r.srcCol).toList.map(tf => Ref(r, f, src, tf)))
  }.groupBy(_.r.name).values.map(_.headOption.getOrElse(throw new IllegalStateException())).toSeq.sortBy(_.r.name).sortBy(x => x.src.title -> x.tf.title)
}

final case class ExportModelReference(name: String, propertyName: String = "", srcTable: String, srcCol: String, tgt: String, notNull: Boolean)
