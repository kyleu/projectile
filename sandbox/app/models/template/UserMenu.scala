package models.template

import com.kyleu.projectile.models.config.{MenuProvider, NavMenu}
import com.kyleu.projectile.models.user.SystemUser

object UserMenu extends MenuProvider {
  private[this] lazy val staticSandbox = NavMenu(key = "sandbox", title = "Sandbox", url = Some(controllers.routes.HomeController.sandbox().url))

  private[this] lazy val staticMenu = Seq(staticSandbox) ++ ComponentMenu.menu :+ NavMenu.system

  override def adminMenu(u: SystemUser) = staticMenu
}
