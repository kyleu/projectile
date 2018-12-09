package com.projectile.util

import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}

import better.files.File
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.decoding.ConfiguredDecoder
import io.circe.generic.extras.encoding.ConfiguredObjectEncoder
import io.circe.java8.time._
import shapeless.Lazy

import scala.language.implicitConversions
import scala.util.control.NonFatal

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

  implicit val circeConfiguration: Configuration = Configuration.default.withDefaults

  def deriveDecoder[A](implicit decode: Lazy[ConfiguredDecoder[A]]) = io.circe.generic.extras.semiauto.deriveDecoder[A]
  def deriveEncoder[A](implicit decode: Lazy[ConfiguredObjectEncoder[A]]) = io.circe.generic.extras.semiauto.deriveEncoder[A]
  def deriveFor[A](implicit decode: Lazy[ConfiguredDecoder[A]]) = io.circe.generic.extras.semiauto.deriveFor[A]

  implicit def encoderOps[A](a: A): io.circe.syntax.EncoderOps[A] = io.circe.syntax.EncoderOps[A](a)

  // Jackson
  def parseJson(s: String) = io.circe.jackson.parse(s)
  def decodeJson[A](s: String)(implicit decoder: Decoder[A]) = try {
    io.circe.jackson.decode[A](s)
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Error [${x.getMessage}] parsing json: $s", x)
  }
  def printJson(j: Json) = io.circe.jackson.jacksonPrint(j)

  // Jawn
  // def parseJson(s: String) = io.circe.parser.parse(s)
  // def decodeJson[A](s: String)(implicit decoder: Decoder[A]) = io.circe.parser.decode[A](s)
  // def printJson(j: Json) = io.circe.Printer.spaces2.pretty(j)

  def extract[T: Decoder](json: Json) = json.as[T] match {
    case Right(u) => u
    case Left(x) => throw x
  }

  def loadFile[T: Decoder](f: File, ctx: String) = if (f.exists && f.isRegularFile && f.isReadable) {
    decodeJson[T](f.contentAsString) match {
      case Right(is) => is
      case Left(x) => throw new IllegalStateException(s"Error loading [$ctx] from [${f.pathAsString}]: ${x.getMessage}", x)
    }
  } else {
    throw new IllegalStateException(s"Cannot load [${f.pathAsString}] for [$ctx]")
  }
}