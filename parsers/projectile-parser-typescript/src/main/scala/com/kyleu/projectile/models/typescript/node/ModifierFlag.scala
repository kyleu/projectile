package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.typescript.node.SyntaxKind._
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ModifierFlag(override val value: String, val kinds: SyntaxKind*) extends StringEnumEntry {
  def scalaKeyword = false
  override def toString = value
}

object ModifierFlag extends StringEnum[ModifierFlag] with StringCirceEnum[ModifierFlag] {
  case object Abstract extends ModifierFlag("abstract", AbstractKeyword) {
    override def scalaKeyword = true
  }
  case object Declare extends ModifierFlag("declare", DeclareKeyword)
  case object Default extends ModifierFlag("default", DefaultKeyword)
  case object Export extends ModifierFlag("export", ExportKeyword)
  case object Public extends ModifierFlag("public", PublicKeyword) {
    override def scalaKeyword = true
  }
  case object Private extends ModifierFlag("private", PrivateKeyword) {
    override def scalaKeyword = true
  }
  case object Protected extends ModifierFlag("protected", ProtectedKeyword) {
    override def scalaKeyword = true
  }
  case object Readonly extends ModifierFlag("readonly", ReadonlyKeyword) {
    override def scalaKeyword = true
  }
  case object Static extends ModifierFlag("static", StaticKeyword)

  case object Optional extends ModifierFlag("opt", DeclareKeyword)

  override val values = findValues

  val byKind = values.flatMap(v => v.kinds.map(_ -> v)).toMap
}
