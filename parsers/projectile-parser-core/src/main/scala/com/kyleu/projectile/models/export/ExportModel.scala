// scalastyle:off file.size.limit
package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.database.schema.{Column, ForeignKey}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.project.member.ModelMember
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.util.JsonSerializers._

object ExportModel {
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
    arguments: List[ExportField] = Nil,
    fields: List[ExportField],
    defaultOrder: Option[OrderBy] = None,
    pkColumns: List[Column] = Nil,
    foreignKeys: List[ForeignKey] = Nil,
    references: List[ExportModelReference] = Nil,
    features: Set[ModelFeature] = Set.empty,
    extendsClass: Option[String] = None,
    icon: Option[String] = None,
    readOnly: Boolean = false,
    provided: Boolean = false,
    source: Option[String] = None
) {
  def apply(m: ModelMember) = copy(
    pkg = m.pkg.toList,
    propertyName = m.getOverride("propertyName", propertyName),
    className = m.getOverride("className", className),
    title = m.getOverride("title", title),
    plural = m.getOverride("plural", plural),
    features = m.features,
    fields = fields.filterNot(f => m.ignored.contains(f.key)).map(f => f.copy(
      propertyName = m.getOverride(s"${f.key}.propertyName", f.propertyName),
      title = m.getOverride(s"${f.key}.title", f.title),
      t = m.getOverride(s"${f.key}.type", "") match {
        case "" => f.t
        case x => FieldType.withValue(x)
      },
      inLocalSearch = m.getOverride(s"${f.key}.localSearch", m.getOverride(s"${f.key}.search", f.inLocalSearch.toString)).toBoolean,
      inGlobalSearch = m.getOverride(s"${f.key}.globalSearch", f.inGlobalSearch.toString).toBoolean,
      inSummary = m.getOverride(s"${f.key}.summary", f.inSummary.toString).toBoolean
    )),
    defaultOrder = m.getOverride("defaultOrder", "none") match {
      case "none" => None
      case o => o.split("-").toList match {
        case asc :: col :: Nil => Some(OrderBy(col, OrderBy.Direction.fromBoolAsc(asc == "asc")))
        case _ => throw new IllegalStateException(s"Cannot parse [${o}] as default order")
      }
    },
    foreignKeys = foreignKeys.filterNot(fk => m.ignored.contains("fk." + fk.name)).map { fk =>
      fk.copy(propertyName = m.getOverride(s"fk.${fk.name}.propertyName", fk.propertyName))
    },
    references = references.filterNot(r => m.ignored.contains("reference." + r.name)).map { r =>
      r.copy(propertyName = m.getOverride(s"reference.${r.name}.propertyName", r.propertyName))
    },
    provided = m.getOverride("provided", provided.toString) == "true"
  )

  val fullClassName = (pkg :+ className).mkString(".")
  def fullClassPath(config: ExportConfiguration) = (modelPackage(config) :+ className).mkString(".")
  val firstPackage = pkg.headOption.getOrElse("system")
  val propertyPlural = ExportHelper.toIdentifier(plural)

  val pkFields = pkColumns.flatMap(c => getFieldOpt(c.name))
  val nonPkFields = fields.filterNot(pkFields.contains)
  def pkType(config: ExportConfiguration) = pkFields match {
    case Nil => "???"
    case h :: Nil => h.scalaType(config)
    case cols => "(" + cols.map(_.scalaType(config)).mkString(", ") + ")"
  }
  val isSerial = pkFields.nonEmpty && pkFields.forall(_.t == FieldType.SerialType)

  val globalSearchFields = fields.filter(_.inGlobalSearch)
  val localSearchFields = fields.filter(_.inLocalSearch)
  val summaryFields = fields.filter(_.inSummary).filterNot(x => pkFields.exists(_.key == x.key))

  def searchCols = (foreignKeys.flatMap(_.references match {
    case ref :: Nil => Some(ref.source)
    case _ => None
  }) ++ localSearchFields.map(_.key)).distinct.sorted

  private[this] def p(cfg: ExportConfiguration) = if (provided) { cfg.systemPackage } else { cfg.applicationPackage }
  def modelPackage(config: ExportConfiguration) = {
    val prelude = if (inputType.isThrift) { Nil } else { p(config) }
    prelude ++ (pkg.lastOption match {
      case Some("models") => pkg
      case Some("fragment") => pkg
      case Some("input") => pkg
      case Some("mutation") => pkg
      case Some("query") => pkg
      case _ if inputType == InputType.Model.TypeScriptModel => pkg
      case _ => "models" +: pkg
    })
  }
  def queriesPackage(config: ExportConfiguration) = p(config) ++ List("models", "queries") ++ pkg
  def graphqlPackage(config: ExportConfiguration) = p(config) ++ List("models", "graphql") ++ pkg
  def slickPackage(config: ExportConfiguration) = p(config) ++ List("models", "table") ++ pkg
  def doobiePackage(config: ExportConfiguration) = p(config) ++ List("models", "doobie") ++ pkg
  def servicePackage(config: ExportConfiguration) = p(config) ++ List("services") ++ pkg
  def controllerPackage(config: ExportConfiguration) = {
    p(config) ++ List("controllers", "admin") ++ (if (pkg.isEmpty) { List("system") } else { pkg })
  }
  def routesPackage(config: ExportConfiguration) = controllerPackage(config) :+ "routes"
  def viewPackage(config: ExportConfiguration) = p(config) ++ Seq("views", "admin") ++ pkg
  def viewHtmlPackage(config: ExportConfiguration) = p(config) ++ Seq("views", "html", "admin") ++ pkg

  def fullServicePath(config: ExportConfiguration) = s"${(servicePackage(config) :+ className).mkString(".")}Service"
  def injectedService(config: ExportConfiguration) = s"injector.getInstance(classOf[${fullServicePath(config)}])"

  def getField(k: String) = getFieldOpt(k).getOrElse {
    throw new IllegalStateException(s"No field for model [$className] with name [$k]. Available fields: [${fields.map(_.propertyName).mkString(", ")}]")
  }
  def getFieldOpt(k: String) = fields.find(f => f.key == k || f.propertyName == k)
  def perm(act: String) = s"""("${pkg.headOption.getOrElse("system")}", "$className", "$act")"""

  def iconRef(config: ExportConfiguration) = if (provided) {
    s"${(config.systemPackage :+ "models" :+ "web").mkString(".")}.InternalIcons.$propertyName"
  } else {
    s"${(config.applicationPackage :+ "models" :+ "template").mkString(".")}.Icons.$propertyName"
  }

  lazy val isJoinTable = {
    val fkFields = foreignKeys.filter(_.references.size == 1).flatMap(_.references.map(_.source)).distinct
    pkFields.forall(pk => fkFields.contains(pk.key))
  }
}
