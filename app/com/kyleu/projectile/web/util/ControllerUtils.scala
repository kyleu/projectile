package com.kyleu.projectile.web.util

import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.util.JsonSerializers.Json
import com.kyleu.projectile.util.{JsonSerializers, NullUtils}
import play.api.data.FormError
import play.api.mvc.AnyContent

object ControllerUtils {
  def getForm(body: AnyContent, prefix: Option[String] = None) = body.asFormUrlEncoded match {
    case Some(f) =>
      val fullMap = f.map(x => x._1 -> x._2.mkString(","))
      prefix.map(p => fullMap.filter(_._1.startsWith(p)).map(x => x._1.stripPrefix(p) -> x._2)).getOrElse(fullMap).toMap
    case None => throw new IllegalStateException("Missing form post")
  }

  def errorsToString(errors: Seq[FormError]) = errors.map(e => e.key + ": " + e.message).mkString(", ")

  def jsonBody(body: AnyContent) = body.asJson.map { json =>
    JsonSerializers.parseJson(play.api.libs.json.Json.stringify(json)) match {
      case Right(x) => x
      case Left(x) => throw x
    }
  }.getOrElse(throw new IllegalStateException("Http post with json body required"))

  def jsonFormOrBody(body: AnyContent, key: String) = {
    val content = body.asFormUrlEncoded.map(_(key).headOption.getOrElse(throw new IllegalStateException(s"Missing form field [$key]")))
    content.map(JsonSerializers.readJson).getOrElse(jsonBody(body))
  }

  def jsonObject(json: Json) = json.asObject.getOrElse(throw new IllegalStateException("Json is not an object"))

  def jsonArguments(body: AnyContent, arguments: String*) = {
    val json = jsonObject(jsonFormOrBody(body, "arguments"))
    arguments.map(arg => json(arg) match {
      case Some(argJson) => arg -> argJson
      case None => throw new IllegalStateException(s"Missing argument [$arg] in body")
    }).toMap
  }

  def modelForm(rawForm: Map[String, Seq[String]]) = {
    val form = rawForm.map(x => x._1 -> x._2.headOption.getOrElse(throw new IllegalStateException("Empty form field")))
    val fields = form.toSeq.filter(x => x._1.endsWith("-include") && x._2 == "true").map(_._1.stripSuffix("-include"))
    def valFor(f: String) = form.get(f) match {
      case Some(x) if x == NullUtils.str => None
      case Some(x) => Some(x)
      case None => form.get(f + "-date") match {
        case Some(d) if d == "∅" => None
        case Some(d) => Some(s"$d${form.get(f + "-time").filter(_ != "∅").map(" " + _).getOrElse(" 00:00:00")}")
        case None => Some(form.getOrElse(f + "-time", throw new IllegalStateException(s"Cannot find value for included field [$f]"))).filter(_ != "∅")
      }
    }
    fields.map(f => DataField(f, valFor(f)))
  }
}
