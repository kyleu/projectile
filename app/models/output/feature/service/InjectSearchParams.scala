package models.output.feature.service

import models.export.ExportModel

case class InjectSearchParams(model: ExportModel) {
  val viewClass = (model.viewHtmlPackage :+ (model.propertyName + "SearchResult")).mkString(".")
  val message = model.pkFields match {
    case Nil => s"""s"${model.title} matched [$$q].""""
    case cols => s"""s"${model.title} [${cols.map(x => "${model." + x.propertyName + "}").mkString(", ")}] matched [$$q].""""
  }

  override def toString = s"${model.propertyName}"
}
