package com.kyleu.projectile.models.graphql.sandbox

import com.kyleu.projectile.graphql.GraphQLContext
import com.kyleu.projectile.graphql.GraphQLUtils.deriveObjectType
import com.kyleu.projectile.models.sandbox.SandboxTask
import sangria.macros.derive.ObjectTypeName
import sangria.schema.{Argument, Field, ListType, ObjectType, OptionInputType, StringType, fields}
import com.kyleu.projectile.graphql.GraphQLUtils._

object SandboxSchema {
  val sandboxTaskArg = Argument("task", StringType)
  val sandboxArgumentArg = Argument("arg", OptionInputType(StringType))

  implicit val sandboxTaskType: ObjectType[Unit, SandboxTask] = ObjectType("SandboxTask", fields[Unit, SandboxTask](
    Field("id", StringType, resolve = _.value.toString),
    Field("name", StringType, resolve = _.value.name),
    Field("description", StringType, resolve = _.value.description)
  ))

  implicit val sandboxResultType: ObjectType[GraphQLContext, SandboxTask.Result] = deriveObjectType[GraphQLContext, SandboxTask.Result](
    ObjectTypeName("SandboxResult")
  )

  val queryFields = fields[GraphQLContext, Unit](Field(
    name = "sandbox",
    fieldType = ListType(sandboxTaskType),
    resolve = _ => SandboxTask.getAll
  ))

  val mutationFields = fields[GraphQLContext, Unit](
    Field(
      name = "sandbox",
      fieldType = sandboxResultType,
      arguments = sandboxTaskArg :: sandboxArgumentArg :: Nil,
      resolve = c => SandboxTask.get(c.arg(sandboxTaskArg)).run(SandboxTask.Config(c.ctx.tracing, c.ctx.injector, c.arg(sandboxArgumentArg)))(c.ctx.trace)
    )
  )
}
