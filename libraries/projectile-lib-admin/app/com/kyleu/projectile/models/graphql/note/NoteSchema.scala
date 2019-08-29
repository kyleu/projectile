// scalastyle:off file.size.limit
package com.kyleu.projectile.models.graphql.note

import com.kyleu.projectile.graphql.GraphQLUtils.deriveObjectType
import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID

import com.kyleu.projectile.models.note.{Note, NoteResult}
import com.kyleu.projectile.services.note.NoteService
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.schema._

object NoteSchema extends GraphQLSchemaHelper("note") {
  implicit val notePrimaryKeyId: HasId[Note, UUID] = HasId[Note, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = {
    c.getInstance[NoteService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  }
  val noteByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val noteIdArg = Argument("id", uuidType)
  val noteIdSeqArg = Argument("ids", ListInputType(uuidType))

  val noteRelTypeArg = Argument("relType", StringType)
  val noteRelTypeSeqArg = Argument("relTypes", ListInputType(StringType))
  val noteRelPkArg = Argument("relPk", StringType)
  val noteRelPkSeqArg = Argument("relPks", ListInputType(StringType))
  val noteTextArg = Argument("text", StringType)
  val noteTextSeqArg = Argument("texts", ListInputType(StringType))
  val noteAuthorArg = Argument("author", uuidType)
  val noteAuthorSeqArg = Argument("authors", ListInputType(uuidType))
  val noteCreatedArg = Argument("created", localDateTimeType)
  val noteCreatedSeqArg = Argument("createds", ListInputType(localDateTimeType))

  val noteByAuthorRelation = Relation[Note, UUID]("byAuthor", x => Seq(x.author))
  val noteByAuthorFetcher = Fetcher.rel[GraphQLContext, Note, Note, UUID](
    getByPrimaryKeySeq, (c, rels) => c.getInstance[NoteService].getByAuthorSeq(c.creds, rels(noteByAuthorRelation))(c.trace)
  )

  implicit lazy val noteType: sangria.schema.ObjectType[GraphQLContext, Note] = deriveObjectType()

  implicit lazy val noteResultType: sangria.schema.ObjectType[GraphQLContext, NoteResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "note", desc = None, t = OptionType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByPrimaryKey(c.ctx.creds, c.arg(noteIdArg))(td)
    }, noteIdArg),
    unitField(name = "noteSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByPrimaryKeySeq(c.ctx.creds, c.arg(noteIdSeqArg))(td)
    }, noteIdSeqArg),
    unitField(name = "noteSearch", desc = None, t = noteResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[NoteService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "notesByRelType", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByRelType(c.ctx.creds, c.arg(noteRelTypeArg))(td)
    }, noteRelTypeArg),
    unitField(name = "notesByRelTypeSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByRelTypeSeq(c.ctx.creds, c.arg(noteRelTypeSeqArg))(td)
    }, noteRelTypeSeqArg),
    unitField(name = "notesByRelPk", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByRelPk(c.ctx.creds, c.arg(noteRelPkArg))(td)
    }, noteRelPkArg),
    unitField(name = "notesByRelPkSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByRelPkSeq(c.ctx.creds, c.arg(noteRelPkSeqArg))(td)
    }, noteRelPkSeqArg),
    unitField(name = "notesByText", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByText(c.ctx.creds, c.arg(noteTextArg))(td)
    }, noteTextArg),
    unitField(name = "notesByTextSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByTextSeq(c.ctx.creds, c.arg(noteTextSeqArg))(td)
    }, noteTextSeqArg),
    unitField(name = "notesByAuthor", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByAuthor(c.ctx.creds, c.arg(noteAuthorArg))(td)
    }, noteAuthorArg),
    unitField(name = "notesByAuthorSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByAuthorSeq(c.ctx.creds, c.arg(noteAuthorSeqArg))(td)
    }, noteAuthorSeqArg),
    unitField(name = "notesByCreated", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByCreated(c.ctx.creds, c.arg(noteCreatedArg))(td)
    }, noteCreatedArg),
    unitField(name = "notesByCreatedSeq", desc = None, t = ListType(noteType), f = (c, td) => {
      c.ctx.getInstance[NoteService].getByCreatedSeq(c.ctx.creds, c.arg(noteCreatedSeqArg))(td)
    }, noteCreatedSeqArg)
  )

  val noteMutationType = ObjectType(
    name = "NoteMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(noteType), f = (c, td) => {
        c.ctx.getInstance[NoteService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(noteType), f = (c, td) => {
        c.ctx.getInstance[NoteService].update(c.ctx.creds, c.arg(noteIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, noteIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = noteType, f = (c, td) => {
        c.ctx.getInstance[NoteService].remove(c.ctx.creds, c.arg(noteIdArg))(td)
      }, noteIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "note", desc = None, t = noteMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[Note]) = {
    NoteResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
