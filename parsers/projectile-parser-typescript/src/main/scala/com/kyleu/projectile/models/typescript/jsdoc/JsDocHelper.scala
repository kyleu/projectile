package com.kyleu.projectile.models.typescript.jsdoc

import com.kyleu.projectile.models.typescript.node.SyntaxKind
import com.kyleu.projectile.models.typescript.JsonObjectExtensions._
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.JsonObject

object JsDocHelper {
  def parseJsDoc(jsDocObj: JsonObject) = {
    def extractComment(obj: JsonObject) = obj.apply("comment").map(extract[String]).toSeq.flatMap(_.split('\n'))

    def extractTag(node: JsDocNode, tagObj: JsonObject) = tagObj.kind() match {
      case SyntaxKind.JSDocParameterTag => node.copy(params = node.params :+ JsDocNode.Parameter(tagObj.name(), extractComment(tagObj)))
      case SyntaxKind.JSDocReturnTag => node.copy(ret = extractComment(tagObj) ++ node.ret)
      case SyntaxKind.JSDocAugmentsTag => node.copy(ext = node.ext :+ extractObj[JsonObject](tagObj, "class").name("expression"))
      case SyntaxKind.JSDocClassTag => tagObj.name("tagName") match {
        case "class" => node.copy(tags = node.tags + "class")
        case "constructor" => node.copy(tags = node.tags + "constructor")
        case x => throw new IllegalStateException(s"Unhandled class tag [$x]")
      }
      case SyntaxKind.JSDocTypeTag => node.copy(tags = node.tags ++ extractComment(tagObj))
      case SyntaxKind.JSDocTemplateTag => node.copy(unprocessed = node.unprocessed :+ tagObj.asJson)
      case SyntaxKind.JSDocTag => tagObj.name("tagName") match {
        case "since" => node.copy(since = extractComment(tagObj) ++ node.since)
        case "version" => node.copy(version = extractComment(tagObj) ++ node.version)
        case "deprecated" => node.copy(deprecated = extractComment(tagObj) ++ node.deprecated)
        case "interface" => node.copy(interface = extractComment(tagObj) ++ node.interface)
        case "alias" => node.copy(aliases = node.aliases :+ extractComment(tagObj))
        case "example" => node.copy(examples = node.examples :+ extractComment(tagObj))
        case "default" => node.copy(defaults = node.defaults :+ extractComment(tagObj))
        case "see" => node.copy(see = node.see :+ extractComment(tagObj))

        case "abstract" | "virtual" => node.copy(tags = node.tags + "abstract")
        case "async" => node.copy(tags = node.tags + "async")
        case "experimental" => node.copy(tags = node.tags + "experimental")
        case "author" => node.copy(authors = node.authors ++ extractComment(tagObj))

        case "requires" => node.copy(comment = node.comment ++ extractComment(tagObj))
        case "namespace" => node.copy(comment = node.comment ++ extractComment(tagObj))
        case "description" => node.copy(comment = node.comment ++ extractComment(tagObj))
        case "summary" => node.copy(comment = node.comment ++ extractComment(tagObj))
        case "remarks" => node.copy(comment = node.comment ++ extractComment(tagObj))
        case "api" => node.copy(comment = node.comment ++ extractComment(tagObj))

        case _ => node.copy(unprocessed = node.unprocessed :+ tagObj.asJson)
        // case x => throw new IllegalStateException(s"Unhandled JSDocTag [$x] in [${tagObj.asJson.spaces2}]")
      }
      case _ => node.copy(unprocessed = node.unprocessed :+ tagObj.asJson)
      // case x => throw new IllegalStateException(s"Unhandled JsDoc tag [$x] in [${tagObj.asJson.spaces2}]")
    }

    val initial = JsDocNode(comment = extractComment(jsDocObj))
    jsDocObj.apply("tags").map(j => extract[Seq[JsonObject]](j).foldLeft(initial) { case (node, obj) => extractTag(node, obj) }).getOrElse(initial)
  }
}
