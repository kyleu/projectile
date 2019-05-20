package com.kyleu.projectile.models.menu

import com.kyleu.projectile.models.web.InternalIcons
import play.api.mvc.Call

object SystemMenu {
  private[this] var rootMenus = List.empty[(String, String, () => Call, String)]
  def addRootMenu(key: String, title: String, call: => Call, icon: String) = {
    rootMenus = rootMenus :+ ((key, title, () => call, icon))
  }

  private[this] var modelMenus = List.empty[(String, String, () => Call, String)]
  def addModelMenu(key: String, title: String, call: => Call, icon: String) = {
    modelMenus = modelMenus :+ ((key, title, () => call, icon))
  }

  private[this] var toolMenus = List.empty[(String, String, () => Call, String)]
  def addToolMenu(key: String, title: String, call: => Call, icon: String) = {
    toolMenus = toolMenus :+ ((key, title, () => call, icon))
  }

  private[this] def toMenu(x: (String, String, () => Call, String)) = NavMenu(key = x._1, title = x._2, url = Some(x._3().url), icon = Some(x._4))
  def currentMenu = {
    val models = modelMenus.distinct.map(toMenu) match {
      case Nil => Nil
      case menus => Seq(NavMenu(key = "models", title = "Models", icon = Some(InternalIcons.models), children = menus))
    }
    val tools = toolMenus.distinct.map(toMenu) match {
      case Nil => Nil
      case menus => Seq(NavMenu(key = "tools", title = "Tools", icon = Some(InternalIcons.tools), children = menus))
    }
    NavMenu(key = "system", title = "System", children = rootMenus.distinct.map(toMenu) ++ models ++ tools, flatSection = true)
  }
}
