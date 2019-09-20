package com.kyleu.projectile.models.feature.test

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerTestFile {
  def export(config: ExportConfiguration) = {
    val file = ScalaFile(path = OutputPath.ServerSource, config.applicationPackage :+ "controllers" :+ "admin" :+ "test", "TestController")

    file.addImport(Seq("play", "api", "http"), "MimeTypes")
    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(Seq("com", "google", "inject"), "Injector")

    config.addCommonImport(file, "AuthController")
    config.addCommonImport(file, "Application")
    config.addCommonImport(file, "ExecutionContext")

    file.add("@javax.inject.Singleton")
    file.add("class TestController @javax.inject.Inject() (", 2)

    file.add("override val app: Application, inj: Injector")

    file.add(""")(implicit ec: ExecutionContext) extends AuthController("test") {""", -2)
    file.indent()
    file.add("""def test(t: Option[String] = None) = withSession("test") { implicit request => _ =>""", 1)
    file.add("Future.successful(renderChoice(t) {", 1)
    file.add("""case MimeTypes.HTML => Ok("TODO!")""")
    file.add("""case MimeTypes.JSON => Ok("TODO.json!")""")
    file.add("})", -1)
    file.add("}", -1)

    /*

        val modelF = svc.getByPrimaryKey(request, id)
        val notesF = noteSvc.getFor(request, "TopRow", id)

        notesF.flatMap(notes => modelF.map {
          case Some(model) => renderChoice(t) {
            case MimeTypes.HTML => Ok(views.html.admin.t.topRowView(app.cfg(u = Some(request.identity), "t", "top", model.id.toString), model, notes, app.config.debug))
            case MimeTypes.JSON => Ok(model.asJson)
          }
          case None => NotFound(s"No TopRow found with id [$id]")
        })
      }

     */

    file.add("}", -1)
    file
  }
}
