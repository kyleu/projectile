package com.kyleu.projectile.controllers.help

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.help.routes.HelpEntryController
import com.kyleu.projectile.models.help.HelpEntry
import com.kyleu.projectile.models.menu.{NavMenu, SystemMenu}
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.help.HelpEntryService

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class HelpEntryController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("help") {
  ApplicationFeature.enable(ApplicationFeature.Help)
  SystemMenu.addTailMenu("help", "Help", Some("The help entries for this application"), HelpEntryController.root(), InternalIcons.help)

  def root() = entry("")

  def entry(path: String) = withoutSession("testbed") { implicit request => implicit td =>
    val segments = path.split("/").map(_.trim).filter(_.nonEmpty).toList
    val cfg = app.cfg(request.identity, (if (segments.isEmpty) { Seq("system", "help") } else { segments }): _*)
    val root = NavMenu(key = "_root", title = cfg.projectName, description = Some("The home page of this application"), url = Some("/"), children = cfg.menu)
    val result = segments.foldLeft((Seq.empty[String], root)) { (l, r) =>
      l._2.children.find(_.key == r) match {
        case Some(item) => (l._1 :+ r, item)
        case None => throw new IllegalStateException(s"Cannot load menu with path [$path], possible permissions problem")
      }
    }

    val entry = toHelpEntry(result._2, segments, HelpEntryService.contentFor(segments: _*))

    Future.successful(render {
      case Accepts.Html() => Ok(com.kyleu.projectile.views.html.help.help(cfg, entry))
      case Accepts.Json() => Ok(com.kyleu.projectile.util.JsonSerializers.encoderOps(entry).asJson)
    })
  }

  private[this] def toHelpEntry(ni: NavMenu, path: List[String], content: Option[String] = None): HelpEntry = HelpEntry(
    path = path, title = ni.title, icon = ni.icon, url = ni.url, description = ni.description,
    content = content, children = ni.children.map(c => toHelpEntry(c, path :+ c.key)).toList
  )
}
