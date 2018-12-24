package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.database.schema.{Column, ForeignKey}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.project.member.ModelMember
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.util.JsonSerializers._

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
    inputType: InputType.Model,
    key: String,
    pkg: List[String] = Nil,
    propertyName: String,
    className: String,
    title: String,
    description: Option[String],
    plural: String,
    arguments: List[ExportField],
    fields: List[ExportField],
    pkColumns: List[Column] = Nil,
    foreignKeys: List[ForeignKey] = Nil,
    references: List[ExportModel.Reference] = Nil,
    features: Set[ModelFeature] = Set.empty,
    extendsClass: Option[String] = None,
    icon: Option[String] = None,
    readOnly: Boolean = false,
    source: Option[String] = None
) {
  def apply(m: ModelMember) = copy(
    pkg = m.pkg.toList,
    propertyName = m.getOverride("propertyName", propertyName),
    className = m.getOverride("className", className),
    title = m.getOverride("title", title),
    plural = m.getOverride("plural", plural),
    features = m.features,
    fields = fields.filterNot(f => m.ignored.contains(f.key)).map { f =>
      f.copy(
        propertyName = m.getOverride(s"${f.key}.propertyName", f.propertyName),
        title = m.getOverride(s"${f.key}.title", f.title),
        inSearch = m.getOverride(s"${f.key}.search", f.inSearch.toString).toBoolean
      )
    },
    foreignKeys = foreignKeys.filterNot(fk => m.ignored.contains("fk." + fk.name)).map { fk =>
      fk.copy(propertyName = m.getOverride(s"fk.${fk.name}.propertyName", fk.propertyName))
    },
    references = references.filterNot(r => m.ignored.contains("reference." + r.name)).map { r =>
      r.copy(propertyName = m.getOverride(s"reference.${r.name}.propertyName", r.propertyName))
    }
  )

  val fullClassName = (pkg :+ className).mkString(".")

  def fullClassPath(config: ExportConfiguration) = (modelPackage(config) :+ className).mkString(".")

  val propertyPlural = ExportHelper.toIdentifier(plural)

  val pkFields = pkColumns.flatMap(c => getFieldOpt(c.name))
  def pkType(config: ExportConfiguration) = pkFields match {
    case Nil => "???"
    case h :: Nil => h.scalaType(config)
    case cols => "(" + cols.map(_.scalaType(config)).mkString(", ") + ")"
  }

  val indexedFields = fields.filter(_.indexed).filterNot(_.t == FieldType.TagsType)
  val searchFields = fields.filter(_.inSearch)

  val extraFields = searchFields.filterNot(pkFields.contains).filter {
    case x if x.t == FieldType.TagsType => false
    case _ => true
  }

  val summaryFields = fields.filter(_.inSummary).filterNot(x => pkFields.exists(_.key == x.key))

  def modelPackage(config: ExportConfiguration) = {
    val prelude = if (inputType.isThrift) { Nil } else { config.applicationPackage }
    prelude ++ (pkg.lastOption match {
      case Some("models") => pkg
      case Some("fragment") => pkg
      case Some("input") => pkg
      case Some("mutation") => pkg
      case Some("query") => pkg
      case _ => "models" +: pkg
    })
  }

  val queriesPackage = List("models", "queries") ++ pkg
  val slickPackage = List("models", "table") ++ pkg
  val doobiePackage = List("models", "doobie") ++ pkg

  val servicePackage = List("services") ++ pkg

  val controllerPackage = List("controllers", "admin") ++ (if (pkg.isEmpty) { List("system") } else { pkg })
  val routesPackage = controllerPackage :+ "routes"

  val viewPackage = Seq("views", "admin") ++ pkg
  val viewHtmlPackage = Seq("views", "html", "admin") ++ pkg

  val serviceReference = pkg match {
    case Nil => "services." + propertyName + "Service"
    case _ => "services." + pkg.head + "Services." + propertyName + "Service"
  }

  def validReferences(config: ExportConfiguration) = {
    references.filter(ref => config.getModelOpt(ref.srcTable).isDefined)
  }
  def transformedReferences(config: ExportConfiguration) = validReferences(config).flatMap { r =>
    val src = config.getModel(r.srcTable, s"$className reference ${r.name}")
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
