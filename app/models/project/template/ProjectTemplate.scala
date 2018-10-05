package models.project.template

import util.JsonSerializers._

final case class ProjectTemplate(key: String, title: String, description: String)

object ProjectTemplate {
  implicit val jsonEncoder: Encoder[ProjectTemplate] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProjectTemplate] = deriveDecoder

  val simple = ProjectTemplate("simple-play-template", "Simple", "A simple Scala Play Framework application with some useful defaults and helper classes")
  val boilerplay = ProjectTemplate("boilerplay", "Boilerplay", "Constantly updated, Boilerplay is a starter web application with loads of features")
  val custom = ProjectTemplate("custom", "Custom", "A custom template allows you to specify default options manually")

  val all = Seq(simple, boilerplay, custom)
}
