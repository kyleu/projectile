package com.kyleu.projectile.graphql

import com.google.inject.Injector
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.reflect.ClassTag

final case class GraphQLContext(
    creds: Credentials,
    tracing: TracingService,
    trace: TraceData,
    injector: Injector
) {
  def getInstance[T](implicit ct: ClassTag[T]): T = injector.getInstance(ct.runtimeClass).asInstanceOf[T]
}
