package com.kyleu.projectile.util

import java.nio.ByteBuffer
import java.time.{LocalDate, LocalDateTime, LocalTime}

import io.circe.Json
import boopickle._

/** Provides all the imports and utility methods you need to work with Boopickle using dates, json, enums, case classes, and sealed traits */
object BinarySerializers extends Base with BasicImplicitPicklers with TransformPicklers with TuplePicklers with MaterializePicklerFallback {
  implicit val jsonPickler: Pickler[Json] = transformPickler((s: String) => JsonSerializers.parseJson(s).right.get)(x => x.spaces2)
  implicit val ldPickler: Pickler[LocalDate] = transformPickler((s: String) => DateUtils.fromDateString(s))(_.toString)
  implicit val ltPickler: Pickler[LocalTime] = transformPickler((s: String) => DateUtils.fromTimeString(s))(_.toString)
  implicit val ldtPickler: Pickler[LocalDateTime] = transformPickler((t: Long) => DateUtils.fromMillis(t))(x => DateUtils.toMillis(x))

  def toByteArray(bb: ByteBuffer) = {
    val arr = new Array[Byte](bb.remaining)
    bb.get(arr)
    arr
  }

  def read[T: Pickler](data: ByteBuffer) = Unpickle[T].fromBytes(data)
  def write[T: Pickler](data: ByteBuffer) = Unpickle[T].fromBytes(data)
}
