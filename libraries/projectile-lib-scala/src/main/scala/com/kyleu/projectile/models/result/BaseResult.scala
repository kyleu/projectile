package com.kyleu.projectile.models.result

import java.time.LocalDateTime

import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.models.result.paging.PagingOptions

abstract class BaseResult[T] {
  def paging: PagingOptions
  def filters: Seq[Filter]
  def orderBys: Seq[OrderBy]
  def totalCount: Int
  def results: Seq[T]
  def durationMs: Int
  def occurred: LocalDateTime
}
