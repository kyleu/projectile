package com.kyleu.projectile.util

import com.kyleu.projectile.util.JsonSerializers.{Decoder, Json}

import scala.util.control.NonFatal

object JacksonUtils {
  // Jackson
  def parseJackson(s: String) = io.circe.jackson.parse(s)
  def decodeJackson[A](s: String)(implicit decoder: Decoder[A]) = try {
    io.circe.jackson.decode[A](s)
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Error [${x.getMessage}] parsing json: $s", x)
  }
  def printJackson(j: Json) = io.circe.jackson.jacksonPrint(j)

  def extract[T: Decoder](json: Json) = json.as[T] match {
    case Right(u) => u
    case Left(x) => throw x
  }

  def extractString[T: Decoder](s: String) = extract[T](parseJackson(s) match {
    case Right(u) => u
    case Left(x) => throw x
  })
}
