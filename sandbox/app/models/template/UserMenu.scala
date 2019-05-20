package models.template

import com.kyleu.projectile.models.menu.{MenuProvider, NavMenu, SystemMenu}
import com.kyleu.projectile.models.user.SystemUser

object UserMenu extends MenuProvider {
  private[this] lazy val staticTestbed = NavMenu(key = "testbed", title = "Testbed", url = Some(controllers.routes.HomeController.testbed().url))

  private[this] lazy val staticMenu = Seq(staticTestbed) ++ ComponentMenu.menu :+ SystemMenu.currentMenu

  override def adminMenu(u: SystemUser) = standardMenu(u) ++ staticMenu
}
