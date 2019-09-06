package com.kyleu.projectile.models.feedback

import com.kyleu.projectile.models.result.BaseResult
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.result.paging.PagingOptions
import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._
import java.time.LocalDateTime

final case class FeedbackResult(
    override val filters: Seq[Filter] = Nil,
    override val orderBys: Seq[OrderBy] = Nil,
    override val totalCount: Int = 0,
    override val paging: PagingOptions = PagingOptions(),
    override val results: Seq[Feedback] = Nil,
    override val durationMs: Int = 0,
    override val occurred: LocalDateTime = DateUtils.now
) extends BaseResult[Feedback]

object FeedbackResult {
  implicit val jsonEncoder: Encoder[FeedbackResult] = (r: FeedbackResult) => io.circe.Json.obj(
    ("filters", r.filters.asJson),
    ("orderBys", r.orderBys.asJson),
    ("totalCount", r.totalCount.asJson),
    ("paging", r.paging.asJson),
    ("results", r.results.asJson),
    ("durationMs", r.durationMs.asJson),
    ("occurred", r.occurred.asJson)
  )

  implicit val jsonDecoder: Decoder[FeedbackResult] = (c: io.circe.HCursor) => for {
    filters <- c.downField("filters").as[Seq[Filter]]
    orderBys <- c.downField("orderBys").as[Seq[OrderBy]]
    totalCount <- c.downField("totalCount").as[Int]
    paging <- c.downField("paging").as[PagingOptions]
    results <- c.downField("results").as[Seq[Feedback]]
    durationMs <- c.downField("durationMs").as[Int]
    occurred <- c.downField("occurred").as[LocalDateTime]
  } yield FeedbackResult(filters, orderBys, totalCount, paging, results, durationMs, occurred)

  def fromRecords(
    q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None,
    startMs: Long, totalCount: Int, results: Seq[Feedback]
  ) = {
    val paging = PagingOptions.from(totalCount, limit, offset)
    val durationMs = (DateUtils.nowMillis - startMs).toInt
    FeedbackResult(paging = paging, filters = filters, orderBys = orderBys, totalCount = totalCount, results = results, durationMs = durationMs)
  }
}
