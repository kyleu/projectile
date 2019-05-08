package com.kyleu.projectile.models.config

import com.kyleu.projectile.models.user.{Role, SystemUser}

trait MenuProvider {
  def adminMenu(u: SystemUser) = Seq.empty[NavMenu]
  def standardMenu(u: SystemUser) = Seq.empty[NavMenu]
  def guestMenu = Seq.empty[NavMenu]

  def menuFor(user: Option[SystemUser]) = user match {
    case Some(u) if u.role == Role.Admin => adminMenu(u)
    case Some(u) => standardMenu(u)
    case None => guestMenu
  }

  def breadcrumbs(menu: Seq[NavMenu], keys: Seq[String]): Seq[BreadcrumbEntry] = menu.find(m => keys.headOption.contains(m.key)) match {
    case Some(m) if keys.size == 1 => Seq(BreadcrumbEntry(m.key, m.title, m.url))
    case Some(m) => BreadcrumbEntry(m.key, m.title, m.url) +: breadcrumbs(m.children, keys.drop(1))
    case None => keys.toList match {
      case Nil => Nil
      case h :: tail => BreadcrumbEntry(h, h, None) +: breadcrumbs(Nil, tail)
    }
  }
}
