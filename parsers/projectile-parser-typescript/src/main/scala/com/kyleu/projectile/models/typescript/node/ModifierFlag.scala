package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.typescript.node.SyntaxKind._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ModifierFlag(override val value: String, val kinds: SyntaxKind*) extends StringEnumEntry {
  override def toString = value
}

object ModifierFlag extends StringEnum[ModifierFlag] with StringCirceEnum[ModifierFlag] {
  case object Abstract extends ModifierFlag("abstract", AbstractKeyword)
  case object Declare extends ModifierFlag("declare", DeclareKeyword)
  case object Default extends ModifierFlag("default", DefaultKeyword)
  case object Export extends ModifierFlag("export", ExportKeyword)
  case object Public extends ModifierFlag("public", PublicKeyword)
  case object Private extends ModifierFlag("private", PrivateKeyword)
  case object Protected extends ModifierFlag("protected", ProtectedKeyword)
  case object Readonly extends ModifierFlag("readonly", ReadonlyKeyword)
  case object Static extends ModifierFlag("static", StaticKeyword)
  case object Const extends ModifierFlag("const", ConstKeyword)

  case object Optional extends ModifierFlag("opt")

  override val values = findValues

  val byKind = values.flatMap(v => v.kinds.map(_ -> v)).toMap

  def keywordFlags(modifiers: Set[ModifierFlag]) = modifiers.filter {
    case Abstract | Public | Private | Protected => true
    case _ => false
  }.flatMap {
    case x if modifiers(x) => Some(x.value)
    case _ => None
  }.map(_ + " ").mkString
}
