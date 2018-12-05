package com.projectile.models.feature.slick

import com.projectile.models.export.ExportModel
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.typ.FieldType.EnumType
import com.projectile.models.feature.ModelFeature
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object TableFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val path = if (model.features(ModelFeature.Shared)) { OutputPath.SharedSource } else { OutputPath.ServerSource }
    val file = ScalaFile(path = path, dir = config.applicationPackage ++ model.slickPackage, key = model.className + "Table")

    config.addCommonImport(file, "SlickQueryService", "imports", "_")

    model.fields.foreach(_.t match {
      case EnumType(key) =>
        val e = config.getEnum(key)
        file.addImport(config.applicationPackage ++ e.slickPackage :+ s"${e.className}ColumnType", s"${e.propertyName}ColumnType")
      case _ => // noop
    })

    file.add(s"object ${model.className}Table {", 1)
    file.add(s"val query = TableQuery[${model.className}Table]")
    addQueries(config, file, model)
    addReferences(config, file, model)
    file.add("}", -1)
    file.add()

    file.addImport(config.applicationPackage ++ model.modelPackage, model.className)

    file.add(s"""class ${model.className}Table(tag: slick.lifted.Tag) extends Table[${model.className}](tag, "${model.key}") {""", 1)

    addFields(config, model, file)
    file.add()

    if (model.pkFields.size > 1) {
      val pkProps = model.pkFields match {
        case h :: Nil => h.propertyName
        case x => "(" + x.map(_.propertyName).mkString(", ") + ")"
      }
      file.add(s"""val modelPrimaryKey = primaryKey("pk_${model.key}", $pkProps)""")
      file.add()
    }
    if (model.fields.lengthCompare(22) > 0) {
      file.addImport(Seq("shapeless"), "HNil")
      file.addImport(Seq("shapeless"), "Generic")
      file.addImport(Seq("slickless"), "_")

      val fieldStr = model.fields.map(_.propertyName).mkString(" :: ")
      file.addImport(config.applicationPackage ++ model.modelPackage, model.className)
      file.add(s"override val * = ($fieldStr :: HNil).mappedWith(Generic[${model.className}])")
    } else {
      val propSeq = model.fields.map(_.propertyName).mkString(", ")
      file.add(s"override val * = ($propSeq) <> (", 1)
      file.add(s"(${model.className}.apply _).tupled,")
      file.add(s"${model.className}.unapply")
      file.add(")", -1)
    }

    file.add("}", -1)
    file.add()
    file

  }

  private[this] def addFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.addImport(config = config, file = file, pkg = model.slickPackage)
    field.t match {
      case FieldType.TagsType => config.addCommonImport(file, "Tag")
      case FieldType.EnumType(key) => file.addImport(config.applicationPackage ++ Seq("models") ++ config.getEnum(key).pkg, config.getEnum(key).className)
      case _ => // noop
    }
    field.addImport(config, file, Nil)
    val propType = if (field.notNull) { field.scalaType(config) } else { "Option[" + field.scalaType(config) + "]" }
    field.description.foreach(d => file.add("/** " + d + " */"))
    val pkKeys = model.pkFields.map(_.key)
    val aiKeys = model.pkColumns.filter(_.autoIncrement).map(_.name).flatMap(k => model.fields.find(_.key == k)).map(_.key)
    val extra = Seq(
      if (pkKeys.size == 1 && pkKeys.contains(field.key)) { Some(", O.PrimaryKey") } else { None },
      if (aiKeys.contains(field.key)) { Some(", O.AutoInc") } else { None }
    ).flatten.mkString
    file.add(s"""val ${field.propertyName} = column[$propType]("${field.key}"$extra)""")
  }

  private[this] def addQueries(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = {
    model.fields.foreach(_.addImport(config = config, file = file, pkg = model.slickPackage))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        file.add()
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey($colProp: ${field.scalaType(config)}) = query.filter(_.$colProp === $colProp).result.headOption")
        val seqArgs = s"${colProp}Seq: Seq[${field.scalaType(config)}]"
        file.add(s"def getByPrimaryKeySeq($seqArgs) = query.filter(_.$colProp.inSet(${colProp}Seq)).result")
      case fields =>
        file.add()
        val colArgs = fields.map(f => f.propertyName + ": " + f.scalaType(config)).mkString(", ")
        val queryArgs = fields.map(f => "o." + f.propertyName + " === " + f.propertyName).mkString(" && ")
        file.add(s"def getByPrimaryKey($colArgs) = query.filter(o => $queryArgs).result.headOption")
    }
  }

  private[this] def addReferences(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = if (model.foreignKeys.nonEmpty) {
    model.foreignKeys.foreach { fk =>
      fk.references match {
        case h :: Nil =>
          val col = model.fields.find(_.key == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          col.addImport(config = config, file = file, pkg = model.modelPackage)
          val propId = col.propertyName
          val propCls = col.className

          file.add()
          file.add(s"""def getBy$propCls($propId: ${col.scalaType(config)}) = query.filter(_.$propId === $propId).result""")
          file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[${col.scalaType(config)}]) = query.filter(_.$propId.inSet(${propId}Seq)).result""")
        case _ => // noop
      }
    }
  }
}
