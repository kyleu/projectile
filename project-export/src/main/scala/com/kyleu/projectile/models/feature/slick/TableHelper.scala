package com.kyleu.projectile.models.feature.slick

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.file.ScalaFile

object TableHelper {
  def addFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.addImport(config = config, file = file, pkg = model.slickPackage)
    field.t match {
      case FieldType.TagsType => config.addCommonImport(file, "Tag")
      case FieldType.EnumType(key) =>
        val pkg = config.applicationPackage ++ Seq("models") ++ config.getEnum(key, "table file").pkg
        file.addImport(pkg, config.getEnum(key, "table file").className)
      case _ => // noop
    }
    field.addImport(config, file, Nil)
    val propType = if (field.required) { field.scalaType(config) } else { "Option[" + field.scalaType(config) + "]" }
    field.description.foreach(d => file.add("/** " + d + " */"))
    val pkKeys = model.pkFields.map(_.key)
    val aiKeys = model.pkColumns.filter(_.autoIncrement).map(_.name).flatMap(k => model.fields.find(_.key == k)).map(_.key)
    val extra = Seq(
      if (pkKeys.size == 1 && pkKeys.contains(field.key)) { Some(", O.PrimaryKey") } else { None },
      if (aiKeys.contains(field.key)) { Some(", O.AutoInc") } else { None }
    ).flatten.mkString
    file.add(s"""val ${field.propertyName} = column[$propType]("${field.key}"$extra)""")
  }

  def addQueries(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = {
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

  def addReferences(config: ExportConfiguration, file: ScalaFile, model: ExportModel) = if (model.foreignKeys.nonEmpty) {
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
