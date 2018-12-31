package com.kyleu.projectile.graphql

import com.kyleu.projectile.models.note.Note
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

import scala.concurrent.Future

final case class GraphQLContext(creds: Credentials, tracing: TracingService, trace: TraceData, noteLookup: (Credentials, String, Any*) => TraceData => Future[Seq[Note]])
