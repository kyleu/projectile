package models.export

import models.database.schema.{Column, ColumnType, ForeignKey}
import models.export.config.ExportConfiguration
import models.output.ExportHelper
import models.output.feature.Feature
import models.project.member.ProjectMember
import models.project.member.ProjectMember.InputType
import util.JsonSerializers._

object ExportModel {
  object Reference {
    implicit val jsonEncoder: Encoder[Reference] = deriveEncoder
    implicit val jsonDecoder: Decoder[Reference] = deriveDecoder
  }

  case class Reference(name: String, propertyName: String = "", srcTable: String, srcCol: String, tgt: String, notNull: Boolean)

  implicit val jsonEncoder: Encoder[ExportModel] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportModel] = deriveDecoder
}

case class ExportModel(
    inputType: InputType,
    key: String,
    pkg: List[String] = Nil,
    propertyName: String,
    className: String,
    title: String,
    description: Option[String],
    plural: String,
    fields: List[ExportField],
    pkColumns: List[Column],
    foreignKeys: List[ForeignKey],
    references: List[ExportModel.Reference],
    features: Set[Feature] = Set.empty,
    extendsClass: Option[String] = None,
    icon: Option[String] = None,
    ignored: Boolean = false,
    provided: Boolean = false,
    readOnly: Boolean = false
) {
  def apply(m: ProjectMember) = copy(
    pkg = m.pkg.toList,
    propertyName = m.getOverride("propertyName", propertyName),
    className = m.getOverride("className", className),
    title = m.getOverride("title", title),
    plural = m.getOverride("plural", plural),
    features = m.features,
    fields = fields.filterNot(f => m.ignored.contains(f.key)).map { f =>
      f.copy(
        propertyName = m.getOverride(s"${f.key}.propertyName", f.propertyName),
        title = m.getOverride(s"${f.key}.title", f.title)
      )
    }
  )

  val fullClassName = (pkg :+ className).mkString(".")
  val propertyPlural = ExportHelper.toIdentifier(plural)

  val pkFields = pkColumns.flatMap(c => getFieldOpt(c.name))
  def pkType(config: ExportConfiguration) = pkFields match {
    case Nil => "???"
    case h :: Nil => h.scalaType(config)
    case cols => "(" + cols.map(_.scalaType(config)).mkString(", ") + ")"
  }

  val indexedFields = fields.filter(_.indexed).filterNot(_.t == ColumnType.TagsType)
  val searchFields = fields.filter(_.inSearch)

  val summaryFields = fields.filter(_.inSummary).filterNot(x => pkFields.exists(_.key == x.key))

  val modelPackage = List("models") ++ pkg
  val queriesPackage = List("models", "queries") ++ pkg
  val slickPackage = List("models", "table") ++ pkg
  val doobiePackage = List("models", "doobie") ++ pkg

  val servicePackage = List("services") ++ pkg

  val viewPackage = Seq("views", "admin") ++ pkg
  val viewHtmlPackage = Seq("views", "html", "admin") ++ pkg

  def validReferences(config: ExportConfiguration) = {
    references.filter(ref => config.getModelOpt(ref.srcTable).isDefined)
  }
  def transformedReferences(config: ExportConfiguration) = validReferences(config).flatMap { r =>
    val src = config.getModel(r.srcTable)
    getFieldOpt(r.tgt).flatMap(f => src.getFieldOpt(r.srcCol).map(tf => (r, f, src, tf)))
  }.groupBy(_._1.name).values.map(_.head).toSeq.sortBy(_._1.name)

  def transformedReferencesDistinct(config: ExportConfiguration) = {
    transformedReferences(config).groupBy(x => x._2 -> x._3).toSeq.sortBy(_._1._2.className).map(_._2.head)
  }

  def getField(k: String) = getFieldOpt(k).getOrElse {
    throw new IllegalStateException(s"No field for model [$className] with name [$k]. Available fields: [${fields.map(_.propertyName).mkString(", ")}].")
  }
  def getFieldOpt(k: String) = fields.find(f => f.key == k || f.propertyName == k)
}
