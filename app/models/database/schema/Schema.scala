package models.database.schema

import util.JsonSerializers._

object Schema {
  implicit val jsonEncoder: Encoder[Schema] = deriveEncoder
  implicit val jsonDecoder: Decoder[Schema] = deriveDecoder
}

case class Schema(
    schemaName: Option[String],
    catalog: Option[String],
    url: String,
    username: String,
    engineVersion: String,
    timezone: Double,
    enums: Seq[EnumType],
    tables: Seq[Table],
    views: Seq[View],
    procedures: Seq[Procedure],

    detailsLoadedAt: Option[Long] = None
) {
  val id = catalog.orElse(schemaName).getOrElse(username)

  def getTable(name: String) = tables.find(_.name.equalsIgnoreCase(name))
  def getView(name: String) = views.find(_.name.equalsIgnoreCase(name))
  def getProcedure(name: String) = procedures.find(_.name.equalsIgnoreCase(name))
}
