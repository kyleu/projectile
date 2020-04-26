package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.output.file.ScalaFile

object GraphQLObjectWriter {
  def writeObjects(ctx: String, config: ExportConfiguration, file: ScalaFile, fields: List[ExportField]) = {
    def forObj(t: FieldType) = t match {
      case o: FieldType.ObjectType => getObjects(o)
      case _ => Nil
    }

    def getObjects(o: FieldType.ObjectType): Seq[(String, FieldType.ObjectType)] = {
      (o.key -> o) +: o.fields.map(_.t).flatMap {
        case o: FieldType.ObjectType => getObjects(o)
        case FieldType.ListType(t) => forObj(t)
        case FieldType.SetType(t) => forObj(t)
        case FieldType.MapType(k, v) => forObj(k) ++ forObj(v)
        case _ => Nil
      }
    }

    val objs = fields.map(_.t).collect {
      case FieldType.ListType(t) => forObj(t)
      case FieldType.SetType(t) => forObj(t)
      case o: FieldType.ObjectType => getObjects(o)
    }.flatten.groupBy(_._1).map(x => x._1 -> x._2.map(_._2))

    val dupes = objs.filter(o => o._2.distinct.size > 1).keys.toSeq.sorted
    if (dupes.nonEmpty) {
      throw new IllegalStateException(s"Duplicate conflicting references [${dupes.mkString(", ")}] for [$ctx]")
    }

    objs.map(x => x._1 -> x._2.headOption.getOrElse(throw new IllegalStateException())).foreach { o =>
      val cn = ExportHelper.toClassName(o._1)
      file.add(s"object $cn {", 1)

      file.add(s"implicit val jsonEncoder: Encoder[$cn] = (r: $cn) => io.circe.Json.obj(", 1)
      o._2.fields.foreach { f =>
        val comma = if (o._2.fields.lastOption.contains(f)) { "" } else { "," }
        file.add(s"""("${f.k}", r.${f.k}.asJson)$comma""")
      }
      file.add(")", -1)
      file.add()

      file.add(s"implicit val jsonDecoder: Decoder[$cn] = (c: io.circe.HCursor) => for {", 1)
      o._2.fields.foreach { f =>
        val ts = FieldTypeAsScala.asScala(config = config, t = f.t)
        val typ = if (f.req) { ts } else { "Option[" + ts + "]" }
        file.add(s"""${f.k} <- c.downField("${f.k}").as[$typ]""")
      }
      val props = o._2.fields.map(_.k).mkString(", ")
      file.add(s"} yield $cn($props)", -1)

      file.add("}", -1)
      file.add(s"final case class $cn(", 2)
      GraphQLObjectHelper.addObjectFields(config, file, o._2.fields)
      file.add(")", -2)
      file.add()
    }
    objs.size
  }
}
