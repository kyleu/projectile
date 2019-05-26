package com.kyleu.projectile.models.graphql.user

import java.util.UUID

import com.kyleu.projectile.graphql.GraphQLUtils._
import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.models.graphql.note.NoteSchema
import com.kyleu.projectile.models.user.{SystemUser, SystemUserResult}
import com.kyleu.projectile.services.user.SystemUserService
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object SystemUserSchema extends GraphQLSchemaHelper("systemUser") {
  implicit val systemUserPrimaryKeyId: HasId[SystemUser, UUID] = HasId[SystemUser, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = {
    c.getInstance[SystemUserService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  }
  val systemUserByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val systemUserIdArg = Argument("id", uuidType)
  val systemUserIdSeqArg = Argument("ids", ListInputType(uuidType))

  val systemUserUsernameArg = Argument("username", StringType)
  val systemUserUsernameSeqArg = Argument("usernames", ListInputType(StringType))
  val systemUserProviderArg = Argument("provider", StringType)
  val systemUserProviderSeqArg = Argument("providers", ListInputType(StringType))
  val systemUserKeyArg = Argument("key", StringType)
  val systemUserKeySeqArg = Argument("keys", ListInputType(StringType))

  implicit lazy val systemUserType: sangria.schema.ObjectType[GraphQLContext, SystemUser] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "authoredNotes",
        fieldType = ListType(NoteSchema.noteType),
        resolve = c => NoteSchema.noteByAuthorFetcher.deferRelSeq(
          NoteSchema.noteByAuthorRelation, c.value.id
        )
      )
    ),
    sangria.macros.derive.ReplaceField("profile", Field(name = "profile", fieldType = StringType, resolve = c => c.value.profile.providerID))
  )

  implicit lazy val systemUserResultType: sangria.schema.ObjectType[GraphQLContext, SystemUserResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "systemUser", desc = None, t = OptionType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByPrimaryKey(c.ctx.creds, c.arg(systemUserIdArg))(td)
    }, systemUserIdArg),
    unitField(name = "systemUserSeq", desc = None, t = ListType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByPrimaryKeySeq(c.ctx.creds, c.arg(systemUserIdSeqArg))(td)
    }, systemUserIdSeqArg),
    unitField(name = "systemUserSearch", desc = None, t = systemUserResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[SystemUserService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "systemUserByUsername", desc = None, t = OptionType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByUsername(c.ctx.creds, c.arg(systemUserUsernameArg))(td).map(_.headOption)
    }, systemUserUsernameArg),
    unitField(name = "systemUsersByUsernameSeq", desc = None, t = ListType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByUsernameSeq(c.ctx.creds, c.arg(systemUserUsernameSeqArg))(td)
    }, systemUserUsernameSeqArg),
    unitField(name = "systemUserByProvider", desc = None, t = OptionType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByProvider(c.ctx.creds, c.arg(systemUserProviderArg))(td).map(_.headOption)
    }, systemUserProviderArg),
    unitField(name = "systemUsersByProviderSeq", desc = None, t = ListType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByProviderSeq(c.ctx.creds, c.arg(systemUserProviderSeqArg))(td)
    }, systemUserProviderSeqArg),
    unitField(name = "systemUserByKey", desc = None, t = OptionType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByKey(c.ctx.creds, c.arg(systemUserKeyArg))(td).map(_.headOption)
    }, systemUserKeyArg),
    unitField(name = "systemUsersByKeySeq", desc = None, t = ListType(systemUserType), f = (c, td) => {
      c.ctx.getInstance[SystemUserService].getByKeySeq(c.ctx.creds, c.arg(systemUserKeySeqArg))(td)
    }, systemUserKeySeqArg)
  )

  val systemUserMutationType = ObjectType(
    name = "SystemUserMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(systemUserType), f = (c, td) => {
        c.ctx.getInstance[SystemUserService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(systemUserType), f = (c, td) => {
        c.ctx.getInstance[SystemUserService].update(c.ctx.creds, c.arg(systemUserIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, systemUserIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = systemUserType, f = (c, td) => {
        c.ctx.getInstance[SystemUserService].remove(c.ctx.creds, c.arg(systemUserIdArg))(td)
      }, systemUserIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "systemUser", desc = None, t = systemUserMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[SystemUser]) = {
    SystemUserResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
