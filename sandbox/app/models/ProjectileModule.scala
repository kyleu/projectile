package models

import com.google.inject.Provides
import com.kyleu.projectile.models.AdminModule
import com.kyleu.projectile.models.auth.AuthEnv
import com.kyleu.projectile.models.config.NavMenu
import com.kyleu.projectile.models.user.{Role, SystemUser}
import javax.inject.Singleton
import models.template.UserMenu
import util.Version

class ProjectileModule extends AdminModule(allowSignup = true, initialRole = Role.Admin) {

  @Provides @Singleton
  def providesAuthEnv() = new AuthEnv {}

  override def projectName = Version.projectName
  override def guestMenu = UserMenu.guestMenu
  override def userMenu(u: SystemUser) = if (u.isAdmin) { UserMenu.adminMenu(u) } else { UserMenu.standardMenu(u) }
  override def breadcrumbs(menu: Seq[NavMenu], keys: Seq[String]) = UserMenu.breadcrumbs(menu, keys)
}
