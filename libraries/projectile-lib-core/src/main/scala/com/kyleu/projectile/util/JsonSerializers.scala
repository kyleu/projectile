package com.kyleu.projectile.util

import io.circe.JsonObject
import io.circe.generic.extras
import shapeless.Lazy

import scala.language.implicitConversions

/** Provides all the imports and utility methods you need to work with Circe using dates, uuids, enums, case classes, and sealed traits */
object JsonSerializers {
  type Decoder[A] = io.circe.Decoder[A]
  type Encoder[A] = io.circe.Encoder[A]

  type Json = io.circe.Json

  implicit val circeConfiguration: extras.Configuration = extras.Configuration.default.withDefaults
  def deriveDecoder[A](implicit decode: Lazy[extras.decoding.ConfiguredDecoder[A]]) = extras.semiauto.deriveConfiguredDecoder[A]
  def deriveEncoder[A](implicit encode: Lazy[extras.encoding.ConfiguredAsObjectEncoder[A]]) = extras.semiauto.deriveConfiguredEncoder[A]

  // implicit val magnoliaConfiguration: io.circe.magnolia.configured.Configuration = io.circe.magnolia.configured.Configuration.default.withDefaults
  // def deriveDecoder[A] = io.circe.magnolia.configured.decoder.semiauto.deriveConfiguredMagnoliaDecoder[A]
  // def deriveEncoder[A] = io.circe.magnolia.configured.encoder.semiauto.deriveConfiguredMagnoliaEncoder[A]

  implicit def encoderOps[A](a: A): io.circe.syntax.EncoderOps[A] = io.circe.syntax.EncoderOps[A](a)

  def parseJson(s: String) = io.circe.parser.parse(s)
  def decodeJson[A](s: String)(implicit decoder: Decoder[A]) = io.circe.parser.decode[A](s)
  def printJson(j: Json) = io.circe.Printer.spaces2.pretty(j)

  def extract[T: Decoder](json: Json) = json.as[T] match {
    case Right(x) => x
    case Left(x) => throw x
  }

  @scala.annotation.tailrec
  def extractObj[T: Decoder](obj: JsonObject, key: String): T = key.split('.').toList match {
    case h :: Nil => obj.apply(h).map(extract[T]).getOrElse(throw new IllegalStateException(s"No [$key] field among candidates [${obj.keys.mkString(", ")}]"))
    case h :: x =>
      val next = obj.apply(h).map(extract[JsonObject]).getOrElse {
        throw new IllegalStateException(s"No [$key] path among candidates [${obj.keys.mkString(", ")}]")
      }
      extractObj[T](next, x.mkString("."))
    case Nil => throw new IllegalStateException("No contents")
  }
}
