package models.template

import com.kyleu.projectile.models.menu.NavMenu

object ComponentMenu {
  val menu: Seq[NavMenu] = Nil ++
    /* Start component menu items */
    /* Projectile export section [sandbox] */
    Seq(NavMenu(key = "system", title = "System", url = None, icon = Some(models.template.Icons.pkg_system), children = Seq(
      NavMenu(key = "bottom", title = "Bottoms", url = Some(controllers.admin.system.routes.BottomRowController.list().url), icon = Some(models.template.Icons.bottomRow)),
      NavMenu(key = "top", title = "Tops", url = Some(controllers.admin.system.routes.TopRowController.list().url), icon = Some(models.template.Icons.topRow))
    ))) ++
    /* End component menu items */
    Nil
}
