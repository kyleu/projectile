package com.kyleu.projectile.models.feature.core.graphql

import com.kyleu.projectile.models.export.ExportField
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala, FieldTypeImports, ObjectField}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.output.file.ScalaFile

object GraphQLObjectHelper {
  def addFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ExportField]) = {
    fields.foreach { f =>
      f.addImport(config, file, Nil)
      val t = if (f.required) {
        FieldTypeAsScala.asScala(config, f.t)
      } else {
        s"Option[${FieldTypeAsScala.asScala(config, f.t)}]"
      }
      val param = s"${f.propertyName}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def addObjectFields(config: ExportConfiguration, file: ScalaFile, fields: Seq[ObjectField]) = {
    fields.foreach { f =>
      FieldTypeImports.imports(config, f.t).foreach(pkg => file.addImport(pkg.init, pkg.lastOption.getOrElse(throw new IllegalStateException())))
      val t = if (f.req) {
        FieldTypeAsScala.asScala(config, f.t)
      } else {
        s"Option[${FieldTypeAsScala.asScala(config, f.t)}]"
      }
      val param = s"${f.k}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def addArguments(config: ExportConfiguration, file: ScalaFile, arguments: Seq[ExportField]) = if (arguments.nonEmpty) {
    file.addImport(Seq("io", "circe"), "Json")
    val args = arguments.map { f =>
      f.addImport(config, file, Nil)
      val typ = FieldTypeAsScala.asScala(config, f.t)
      val (t, dv) = if (f.required) {
        typ -> f.defaultValue.map(" = " + _).getOrElse("")
      } else {
        s"Option[$typ]" -> f.defaultValue.map(" = Some(" + _ + ")").getOrElse(" = None")
      }
      s"${f.propertyName}: $t$dv"
    }.mkString(", ")
    val varsDecl = arguments.map(v => s""""${v.propertyName}" -> ${v.propertyName}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
    file.add()
  }

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
      file.add(s"case class $cn(", 2)
      GraphQLObjectHelper.addObjectFields(config, file, o._2.fields)
      file.add(")", -2)
      file.add()
    }
    objs.size
  }
}
