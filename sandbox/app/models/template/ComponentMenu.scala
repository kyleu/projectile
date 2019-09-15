package models.template

import com.kyleu.projectile.models.menu.NavMenu

object ComponentMenu {
  val menu: Seq[NavMenu] = Nil ++
    /* Start component menu items */
    /* Projectile export section [sandbox] */
    Seq(NavMenu(key = "b", title = "B", url = None, icon = Some(models.template.Icons.pkg_b), children = Seq(
      NavMenu(key = "bottom", title = "Bottoms", url = Some(controllers.admin.b.routes.BottomRowController.list().url), icon = Some(models.template.Icons.bottomRow))
    ))) ++
    Seq(NavMenu(key = "t", title = "T", url = None, icon = Some(models.template.Icons.pkg_t), children = Seq(
      NavMenu(key = "top", title = "Tops", url = Some(controllers.admin.t.routes.TopRowController.list().url), icon = Some(models.template.Icons.topRow))
    ))) ++
    /* End component menu items */
    Nil
}
