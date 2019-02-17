package com.kyleu.projectile.util

import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}

import io.circe.java8.time._
import shapeless.Lazy

import scala.language.implicitConversions

/** Provides all the imports and utility methods you need to work with Circe using dates, uuids, enums, case classes, and sealed traits */
object JsonSerializers {
  type Decoder[A] = io.circe.Decoder[A]
  type Encoder[A] = io.circe.Encoder[A]

  type Json = io.circe.Json

  implicit def encodeZonedDateTime: Encoder[ZonedDateTime] = io.circe.java8.time.encodeZonedDateTime
  implicit def encodeLocalDateTime: Encoder[LocalDateTime] = io.circe.java8.time.encodeLocalDateTime
  implicit def encodeLocalDate: Encoder[LocalDate] = io.circe.java8.time.encodeLocalDate
  implicit def encodeLocalTime: Encoder[LocalTime] = io.circe.java8.time.encodeLocalTime

  implicit def decodeZonedDateTime: Decoder[ZonedDateTime] = io.circe.java8.time.decodeZonedDateTime
  implicit def decodeLocalDateTime: Decoder[LocalDateTime] = io.circe.java8.time.decodeLocalDateTime
  implicit def decodeLocalDate: Decoder[LocalDate] = io.circe.java8.time.decodeLocalDate
  implicit def decodeLocalTime: Decoder[LocalTime] = io.circe.java8.time.decodeLocalTime

  implicit val circeConfiguration: io.circe.generic.extras.Configuration = io.circe.generic.extras.Configuration.default.withDefaults
  def deriveDecoder[A](implicit decode: Lazy[io.circe.generic.extras.decoding.ConfiguredDecoder[A]]) = io.circe.generic.extras.semiauto.deriveDecoder[A]
  def deriveEncoder[A](implicit encode: Lazy[io.circe.generic.extras.encoding.ConfiguredObjectEncoder[A]]) = io.circe.generic.extras.semiauto.deriveEncoder[A]

  // implicit val magnoliaConfiguration: io.circe.magnolia.configured.Configuration = io.circe.magnolia.configured.Configuration.default.withDefaults
  // def deriveDecoder[A] = io.circe.magnolia.configured.decoder.semiauto.deriveConfiguredMagnoliaDecoder[A]
  // def deriveEncoder[A] = io.circe.magnolia.configured.encoder.semiauto.deriveConfiguredMagnoliaEncoder[A]

  implicit def encoderOps[A](a: A): io.circe.syntax.EncoderOps[A] = io.circe.syntax.EncoderOps[A](a)

  def parseJson(s: String) = io.circe.parser.parse(s)
  def decodeJson[A](s: String)(implicit decoder: Decoder[A]) = io.circe.parser.decode[A](s)
  def printJson(j: Json) = io.circe.Printer.spaces2.pretty(j)
}
