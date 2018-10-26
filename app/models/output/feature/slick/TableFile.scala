package models.output.feature.slick

import models.database.schema.ColumnType
import models.export.{ExportEnum, ExportModel}
import models.export.config.ExportConfiguration
import models.output.OutputPath
import models.output.file.ScalaFile

object TableFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = model.slickPackage, key = model.className + "Table")

    file.addImport((config.systemPackage ++ Seq("services", "database", "SlickQueryService", "imports")).mkString("."), "_")

    model.fields.foreach(_.enumOpt(config).foreach { e =>
      file.addImport(s"${(config.applicationPackage ++ e.slickPackage).mkString(".")}.${e.className}ColumnType", s"${e.propertyName}ColumnType")
    })

    file.add(s"object ${model.className}Table {", 1)
    file.add(s"val query = TableQuery[${model.className}Table]")
    addQueries(config, file, model)
    addReferences(config, file, model)
    file.add("}", -1)
    file.add()

    file.add(s"""class ${model.className}Table(tag: Tag) extends Table[${model.modelClass}](tag, "${model.name}") {""", 1)

    addFields(config, model, file, config.enums)
    file.add()

    if (model.pkFields.nonEmpty) {
      val pkProps = model.pkFields match {
        case h :: Nil => h.propertyName
        case x => "(" + x.map(_.propertyName).mkString(", ") + ")"
      }
      file.add(s"""val modelPrimaryKey = primaryKey("pk_${model.name}", $pkProps)""")
      file.add()
    }
    if (model.fields.lengthCompare(22) > 0) {
      file.addImport("shapeless", "HNil")
      file.addImport("shapeless", "Generic")
      file.addImport("slickless", "_")

      val fieldStr = model.fields.map(_.propertyName).mkString(" :: ")
      file.addImport((config.applicationPackage ++ Seq("models") ++ model.pkg).mkString("."), model.className)
      file.add(s"override val * = ($fieldStr :: HNil).mappedWith(Generic[${model.className}])")
    } else {
      val propSeq = model.fields.map(_.propertyName).mkString(", ")
      file.add(s"override val * = ($propSeq) <> (", 1)
      file.add(s"(${model.modelClass}.apply _).tupled,")
      file.add(s"${model.modelClass}.unapply")
      file.add(")", -1)
    }

    file.add("}", -1)
    file.add()
    file

  }

  private[this] def addFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile, enums: Seq[ExportEnum]) = model.fields.foreach { field =>
    field.addImport(config = config, file = file, pkg = model.modelPackage)
    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case ColumnType.TagsType =>
        file.addImport(config.tagsPackage.mkString("."), "Tag")
        s"List[Tag]"
      case _ => field.scalaType(config)
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(s"""val ${field.propertyName} = column[$propType]("${field.columnName}")""")
  }

  private[this] def addQueries(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = {
    model.pkFields.foreach(_.addImport(config = config, file = file, pkg = model.modelPackage))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        file.add()
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey($colProp: ${field.scalaType(config)}) = query.filter(_.$colProp === $colProp).result.headOption")
        val seqArgs = s"${colProp}Seq: Seq[${field.scalaType(config)}]"
        file.add(s"def getByPrimaryKeySeq($seqArgs) = query.filter(_.$colProp.inSet(${colProp}Seq)).result")
      case fields => // multiple columns
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
          val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
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
