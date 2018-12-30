package com.kyleu.projectile.util

import better.files.File
import com.kyleu.projectile.util.JsonSerializers._

object JsonFileLoader {
  def loadFile[T: Decoder](f: File, ctx: String) = if (f.exists && f.isRegularFile && f.isReadable) {
    decodeJson[T](f.contentAsString) match {
      case Right(is) => is
      case Left(x) => throw new IllegalStateException(s"Error loading [$ctx] from [${f.pathAsString}]: ${x.getMessage}", x)
    }
  } else {
    throw new IllegalStateException(s"Cannot load [${f.pathAsString}] for [$ctx]")
  }
}
