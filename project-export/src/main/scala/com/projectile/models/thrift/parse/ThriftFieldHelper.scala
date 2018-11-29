package com.projectile.models.thrift.parse

object ThriftFieldHelper {
  def mapKeyValFor(x: String) = x.drop(4).dropRight(1).split(',').map(_.trim).toList match {
    case key :: rest => key -> rest.mkString(",")
    case other => throw new IllegalStateException(s"TODO: $other")
  }
}
