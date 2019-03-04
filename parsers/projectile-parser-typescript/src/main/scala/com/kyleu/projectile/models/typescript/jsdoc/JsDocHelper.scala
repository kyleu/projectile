package com.kyleu.projectile.models.typescript.jsdoc

import com.kyleu.projectile.models.typescript.node.SyntaxKind
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object JsDocHelper {
  def parseJsDoc(jsDocObj: JsonObject) = {
    def extractComment(obj: JsonObject) = obj.apply("comment").map(extract[String])
    def extractTag(node: JsDocNode, tagObj: JsonObject) = tagObj.kind() match {
      case SyntaxKind.JSDocParameterTag => node.copy(params = node.params :+ JsDocNode.Parameter(tagObj.name(), extractComment(tagObj)))
      case SyntaxKind.JSDocReturnTag => node.copy(ret = extractComment(tagObj).orElse(node.ret))
      case SyntaxKind.JSDocAugmentsTag => node.copy(ext = node.ext :+ extractObj[JsonObject](tagObj, "class").name("expression"))
      case SyntaxKind.JSDocClassTag => tagObj.name("tagName") match {
        case "class" => node.copy(tags = node.tags + "class")
        case "constructor" => node.copy(tags = node.tags + "constructor")
        case x => throw new IllegalStateException(s"Unhandled class tag [$x]")
      }
      case SyntaxKind.JSDocTypeTag => tagObj.name("tagName") match {
        case "type" => node
        case x => throw new IllegalStateException(s"Unhandled type tag [$x]")
      }
      case SyntaxKind.JSDocTag => tagObj.name("tagName") match {
        case "since" => node.copy(since = extractComment(tagObj).orElse(node.since))
        case "version" => node.copy(version = extractComment(tagObj).orElse(node.version))
        case "deprecated" => node.copy(deprecated = extractComment(tagObj).orElse(node.deprecated))
        case "interface" => node.copy(interface = extractComment(tagObj).orElse(node.interface))
        case "alias" => node.copy(aliases = node.aliases :+ extractComment(tagObj).getOrElse(""))
        case "example" => node.copy(examples = node.examples :+ extractComment(tagObj).getOrElse(""))
        case "default" => node.copy(defaults = node.defaults :+ extractComment(tagObj).getOrElse(""))
        case "see" => node.copy(see = node.see :+ extractComment(tagObj).getOrElse(""))

        case "abstract" | "virtual" => node.copy(tags = node.tags + "abstract")
        case "async" => node.copy(tags = node.tags + "async")
        case "experimental" => node.copy(tags = node.tags + "experimental")

        case _ => node.copy(unprocessed = node.unprocessed :+ tagObj.asJson)
        // case x => throw new IllegalStateException(s"Unhandled JSDocTag [$x]")
      }
      case _ => node.copy(unprocessed = node.unprocessed :+ tagObj.asJson)
      // case x => throw new IllegalStateException(s"Unhandled JsDoc tag [$x]")
    }

    val initial = JsDocNode(comment = extractComment(jsDocObj))
    jsDocObj.apply("tags").map(j => extract[Seq[JsonObject]](j).foldLeft(initial) { case (node, obj) => extractTag(node, obj) }).getOrElse(initial)
  }
}
