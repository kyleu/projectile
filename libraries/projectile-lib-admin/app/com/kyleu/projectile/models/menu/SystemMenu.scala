package com.kyleu.projectile.models.menu

import com.kyleu.projectile.models.web.InternalIcons
import play.api.mvc.Call

object SystemMenu {
  private[this] val modelsDesc = "Explore the models of the system"
  private[this] val toolsDesc = "System tools for admin usage"
  private[this] val systemDesc = "System actions available in the app"

  private[this] var rootMenus = List.empty[(String, String, Option[String], () => Call, String)]
  def addRootMenu(key: String, title: String, desc: Option[String], call: => Call, icon: String) = {
    rootMenus = rootMenus :+ ((key, title, desc, () => call, icon))
  }

  private[this] var modelMenus = List.empty[(String, String, Option[String], () => Call, String)]
  def addModelMenu(key: String, title: String, desc: Option[String], call: => Call, icon: String) = {
    modelMenus = modelMenus :+ ((key, title, desc, () => call, icon))
  }

  private[this] var toolMenus = List.empty[(String, String, Option[String], () => Call, String)]
  def addToolMenu(key: String, title: String, desc: Option[String], call: => Call, icon: String) = {
    toolMenus = toolMenus :+ ((key, title, desc, () => call, icon))
  }

  private[this] def toMenu(x: (String, String, Option[String], () => Call, String)) = {
    NavMenu(key = x._1, title = x._2, description = x._3, url = Some(x._4().url), icon = Some(x._5))
  }
  def currentMenu = {
    val models = modelMenus.distinct.map(toMenu) match {
      case Nil => Nil
      case menus => Seq(NavMenu(key = "models", title = "Models", description = Some(modelsDesc), icon = Some(InternalIcons.models), children = menus))
    }
    val tools = toolMenus.distinct.map(toMenu) match {
      case Nil => Nil
      case menus => Seq(NavMenu(key = "tools", title = "Tools", description = Some(toolsDesc), icon = Some(InternalIcons.tools), children = menus))
    }
    NavMenu(key = "system", title = "System", description = Some(systemDesc), children = rootMenus.distinct.map(toMenu) ++ models ++ tools, flatSection = true)
  }
}
