package com.kyleu.projectile.graphql

import com.google.inject.Injector
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}

final case class GraphQLContext(
    creds: Credentials,
    tracing: TracingService,
    trace: TraceData,
    injector: Injector
)
