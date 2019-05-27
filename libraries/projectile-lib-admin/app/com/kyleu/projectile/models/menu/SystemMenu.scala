package com.kyleu.projectile.models.menu

import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import play.api.mvc.Call

object SystemMenu {
  private[this] val modelsDesc = "Explore the models of the system"
  private[this] val toolsDesc = "System tools for admin usage"
  private[this] val systemDesc = "System actions available in the app"

  private[this] var rootMenus = List.empty[(String, String, Option[String], () => Call, String, Seq[(String, String, String)])]
  def addRootMenu(key: String, title: String, desc: Option[String], call: => Call, icon: String, permissions: (String, String, String)*) = {
    rootMenus = rootMenus :+ ((key, title, desc, () => call, icon, permissions))
  }

  private[this] var modelMenus = List.empty[(String, String, Option[String], () => Call, String, Seq[(String, String, String)])]
  def addModelMenu(key: String, title: String, desc: Option[String], call: => Call, icon: String, permissions: (String, String, String)*) = {
    modelMenus = modelMenus :+ ((key, title, desc, () => call, icon, permissions))
  }

  private[this] var toolMenus = List.empty[(String, String, Option[String], () => Call, String, Seq[(String, String, String)])]
  def addToolMenu(key: String, title: String, desc: Option[String], call: => Call, icon: String, permissions: (String, String, String)*) = {
    toolMenus = toolMenus :+ ((key, title, desc, () => call, icon, permissions))
  }

  private[this] def toMenu(x: (String, String, Option[String], () => Call, String, Seq[(String, String, String)])) = {
    NavMenu(key = x._1, title = x._2, description = x._3, url = Some(x._4().url), icon = Some(x._5))
  }

  def currentMenu(role: String) = {
    val models = modelMenus.filter(m => check(role, m._6)).distinct.map(toMenu) match {
      case Nil => Nil
      case menus => Seq(NavMenu(key = "models", title = "Models", description = Some(modelsDesc), icon = Some(InternalIcons.models), children = menus))
    }
    val tools = toolMenus.filter(m => check(role, m._6)).distinct.map(toMenu) match {
      case Nil => Nil
      case menus => Seq(NavMenu(key = "tools", title = "Tools", description = Some(toolsDesc), icon = Some(InternalIcons.tools), children = menus))
    }
    val roots = rootMenus.filter(m => check(role, m._6)).distinct.map(toMenu)
    NavMenu(key = "system", title = "System", description = Some(systemDesc), children = roots ++ models ++ tools, flatSection = true)
  }

  private[this] def check(role: String, perms: Seq[(String, String, String)]) = perms.forall(p => PermissionService.check(role, p._1, p._2, p._3)._1)
}
