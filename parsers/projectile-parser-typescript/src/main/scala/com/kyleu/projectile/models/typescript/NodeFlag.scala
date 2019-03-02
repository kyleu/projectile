package com.kyleu.projectile.models.typescript

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class NodeFlag(val v: Int) extends EnumEntry

object NodeFlag extends Enum[NodeFlag] with CirceEnum[NodeFlag] {
  case object None extends NodeFlag(0)

  case object Let extends NodeFlag(1 << 0) // Variable declaration
  case object Const extends NodeFlag(1 << 1) // Variable declaration
  case object NestedNamespace extends NodeFlag(1 << 2) // Namespace declaration
  case object Synthesized extends NodeFlag(1 << 3) // Node was synthesized during transformation
  case object Namespace extends NodeFlag(1 << 4) // Namespace declaration
  case object ExportContext extends NodeFlag(1 << 5) // Export context (initialized by binding)
  case object ContainsThis extends NodeFlag(1 << 6) // Interface contains references to "this"
  case object HasImplicitReturn extends NodeFlag(1 << 7) // If function implicitly returns on one of codepaths (initialized by binding)
  case object HasExplicitReturn extends NodeFlag(1 << 8) // If function has explicit reachable return on one of codepaths (initialized by binding)
  case object GlobalAugmentation extends NodeFlag(1 << 9) // Set if module declaration is an augmentation for the global scope
  case object HasAsyncFunctions extends NodeFlag(1 << 10) // If the file has async functions (initialized by binding)
  case object DisallowInContext extends NodeFlag(1 << 11) // If node was parsed in a context where 'in-expressions' are not allowed
  case object YieldContext extends NodeFlag(1 << 12) // If node was parsed in the 'yield' context created when parsing a generator
  case object DecoratorContext extends NodeFlag(1 << 13) // If node was parsed as part of a decorator
  case object AwaitContext extends NodeFlag(1 << 14) // If node was parsed in the 'await' context created when parsing an async function
  case object ThisNodeHasError extends NodeFlag(1 << 15) // If the parser encountered an error when parsing the code that created this node
  case object JavaScriptFile extends NodeFlag(1 << 16) // If node was parsed in a JavaScript
  case object ThisNodeOrAnySubNodesHasError extends NodeFlag(1 << 17) // If this node or any of its children had an error
  case object HasAggregatedChildData extends NodeFlag(1 << 18) // If we've computed data from children and cached it in this node
  // case object PossiblyContainsDynamicImport extends NodeFlag(1 << 19)
  // case object PossiblyContainsImportMeta extends NodeFlag(1 << 20)
  case object JSDoc extends NodeFlag(1 << 21) // If node was parsed inside jsdoc
  // case object Ambient extends NodeFlag(1 << 22) // If node was inside an ambient context -- a declaration file, or inside something with the `declare` modifier.
  // case object InWithStatement extends NodeFlag(1 << 23) // If any ancestor of node was the `statement` of a WithStatement (not the `expression`)
  case object JsonFile extends NodeFlag(1 << 24) // If node was parsed in a Json

  override val values = findValues

  def matching(i: Int): Set[NodeFlag] = values.filter(v => (i & v.v) != 0).filterNot(_ == None).toSet
}
