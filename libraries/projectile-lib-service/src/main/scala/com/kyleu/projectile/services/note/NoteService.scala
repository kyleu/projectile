package com.kyleu.projectile.services.note

import com.kyleu.projectile.models.note.Note
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.Future

trait NoteService {
  def getFor(creds: Credentials, model: String, pk: Any*)(implicit trace: TraceData): Future[Seq[Note]]
  def getForSeq(creds: Credentials, models: Seq[(String, String)])(implicit trace: TraceData): Future[Seq[Note]]
  def csvFor(operation: String, totalCount: Int, rows: Seq[Note])(implicit trace: TraceData): String
}
