package com.kyleu.projectile.models.feature.slick

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType.EnumType
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object TableFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, dir = model.slickPackage(config), key = model.className + "Table")

    config.addCommonImport(file, "SlickQueryService", "imports", "_")

    model.fields.foreach(_.t match {
      case EnumType(key) =>
        val e = config.getEnum(key, "table file")
        file.addImport(e.slickPackage(config) :+ s"${e.className}ColumnType", s"${e.propertyName}ColumnType")
      case _ => // noop
    })

    file.add(s"object ${model.className}Table {", 1)
    file.add(s"val query = TableQuery[${model.className}Table]")
    TableHelper.addQueries(config, file, model)
    TableHelper.addReferences(config, file, model)
    TableHelper.addExtensions(config, file, model)
    file.add("}", -1)
    file.add()

    file.addImport(model.modelPackage(config), model.className)

    file.add(s"""class ${model.className}Table(tag: slick.lifted.Tag) extends Table[${model.className}](tag, "${model.key}") {""", 1)

    TableHelper.addFields(config, model, file)
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
      file.addImport(model.modelPackage(config), model.className)
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
}
