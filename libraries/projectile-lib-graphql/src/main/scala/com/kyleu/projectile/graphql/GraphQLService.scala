package com.kyleu.projectile.graphql

import com.google.inject.Injector
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.{TraceData, TracingService}
import com.kyleu.projectile.util.{JsonSerializers, Logging}
import io.circe.Json
import sangria.execution.{ExceptionHandler, Executor, HandledException, QueryReducer}
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import sangria.validation.QueryValidator

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@javax.inject.Singleton
class GraphQLService @javax.inject.Inject() (
    tracing: TracingService, injector: Injector, schema: GraphQLSchema
)(implicit ec: ExecutionContext) extends Logging {
  protected val exceptionHandler = ExceptionHandler {
    case (_, e: IllegalStateException) =>
      log.warn("Error encountered while running GraphQL query", e)(TraceData.noop)
      HandledException(message = e.getMessage, additionalFields = Map.empty)
  }

  private[this] val rejectComplexQueries = QueryReducer.rejectComplexQueries[Any](1000, (_, _) => new IllegalArgumentException("Query is too complex"))

  def executeQuery(
    query: String, variables: Option[Json], operation: Option[String], creds: Credentials, debug: Boolean
  )(implicit t: TraceData) = {
    tracing.trace(s"graphql.service.execute.${operation.getOrElse("adhoc")}") { td =>
      if (!td.isNoop) {
        td.tag("query", query)
        variables.foreach(v => td.tag("variables", v.spaces2))
        operation.foreach(o => td.tag("operation", o))
        td.tag("debug", debug.toString)
      }

      QueryParser.parse(query) match {
        case Success(ast) =>
          td.annotate("parse.success")
          val ctx = GraphQLContext(
            creds = creds,
            tracing = tracing,
            trace = td,
            injector = injector
          )
          val ret = Executor.execute(
            schema = schema.schema,
            queryAst = ast,
            userContext = ctx,
            operationName = operation,
            variables = variables.getOrElse(Json.obj()),
            deferredResolver = schema.resolver,
            exceptionHandler = exceptionHandler,
            maxQueryDepth = Some(100),
            queryValidator = QueryValidator.default,
            queryReducers = List[QueryReducer[GraphQLContext, _]](rejectComplexQueries),
            middleware = Nil // if (debug) { TracingExtension :: Nil } else { Nil }
          )
          ret
        case Failure(error) =>
          td.annotate("parse.failure")
          throw error
      }
    }
  }

  def parseVariables(variables: String) = if (variables.trim == "" || variables.trim == "null") {
    Json.obj()
  } else {
    JsonSerializers.parseJson(variables) match {
      case Right(x) => x
      case Left(_) => Json.obj()
    }
  }
}
