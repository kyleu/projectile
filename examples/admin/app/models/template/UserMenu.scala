package models.template

import com.kyleu.projectile.models.menu.{MenuProvider, NavMenu, SystemMenu}
import com.kyleu.projectile.models.user.SystemUser

object UserMenu extends MenuProvider {
  private[this] lazy val staticTestbed = {
    NavMenu(key = "testbed", title = "Testbed", description = Some("Just new boot goofin'"), url = Some(controllers.routes.HomeController.testbed().url))
  }

  override def userMenu(u: SystemUser) = Seq(staticTestbed) ++ ComponentMenu.menu :+ SystemMenu.currentMenu(u.role)
}
