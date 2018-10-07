package models.input

import io.circe.{HCursor, ObjectEncoder}
import util.JsonSerializers._

object Input {
  implicit val jsonEncoder: Encoder[Input] = new ObjectEncoder[Input] {
    override def encodeObject(n: Input) = {
      val ret = n match {
        case o: PostgresInput => o.asJson.asObject.get
        // case o: Unknown => o.asJson.asObject.get
      }
      ("type" -> n.t.asJson) +: ret
    }
  }

  implicit val jsonDecoder: Decoder[Input] = (c: HCursor) => c.downField("type").as[String] match {
    case Left(x) => throw new IllegalStateException(s"Unable to find [type] among [${c.keys.mkString(", ")}].", x)
    case Right(t) => t match {
      case PostgresInput.t => c.as[PostgresInput]
      // case "" => c.as[Something]
      case _ => throw new IllegalStateException(s"Cannot decode type [$t]")
    }
  }
}

abstract class Input extends Ordered[Input] {
  def t: String
  def key: String
  def title: String
  def description: String

  override def compare(p: Input) = title.compare(p.title)
}
