package com.kyleu.projectile.graphql

import com.google.inject.Injector
import com.kyleu.projectile.models.note.Note
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.Future

final case class GraphQLContext(
    creds: Credentials,
    tracing: TracingService,
    trace: TraceData,
    injector: Injector,
    noteLookup: (Credentials, String, Any*) => TraceData => Future[Seq[Note]]
)
