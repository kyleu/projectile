package com.projectile.models.feature.core.graphql

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.{ExportHelper, OutputPath}
import com.projectile.models.output.file.ScalaFile

object GraphQLOperationFile {
  val includeDefaults = false

  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.pkg, key = model.className)

    config.addCommonImport(file, "JsonSerializers", "_")
    config.addCommonImport(file, "GraphQLQuery")

    file.add(s"object ${model.className} {", 1)

    GraphQLObjectHelper.addArguments(config, file, model.arguments)

    def getObjects(o: FieldType.ObjectType): Seq[(String, FieldType.ObjectType)] = {
      (o.key -> o) +: o.fields.map(_.v).collect {
        case o: FieldType.ObjectType => getObjects(o)
      }.flatten
    }

    val objs = model.fields.map(_.t).collect {
      case o: FieldType.ObjectType => getObjects(o)
    }.flatten.groupBy(_._1).mapValues(_.map(_._2))

    if (objs.exists(_._2.size > 1)) {
      throw new IllegalStateException("TODO: Duplicate references")
    }

    objs.mapValues(_.head).foreach { o =>
      val cn = ExportHelper.toClassName(o._1)
      file.add(s"object $cn {", 1)
      file.add(s"implicit val jsonDecoder: Decoder[$cn] = deriveDecoder")
      file.add("}", -1)
      file.add(s"case class $cn(", 2)
      GraphQLObjectHelper.addObjectFields(config, file, o._2.fields)
      file.add(")", -2)
      file.add()
    }

    file.add("object Data {", 1)
    file.add("implicit val jsonDecoder: Decoder[Data] = deriveDecoder")
    file.add("}", -1)
    file.add("case class Data(", 2)
    GraphQLObjectHelper.addFields(config, file, model.fields)
    file.add(")", -2)

    file.add()
    file.add(s"""val query = new GraphQLQuery[Data]("${model.className}")""")

    file.add("}", -1)
    file.add()

    file
  }
}
