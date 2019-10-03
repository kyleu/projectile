/* Generated File */
import com.kyleu.projectile.models.result.data.DataFieldModel
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

object TestModelSupport {
  private[this] val creds = Credentials.noop
  private[this] implicit val td: TraceData = TraceData.noop

  def insert(m: DataFieldModel) = m match {
    case model: models.size.BigRow => TestServices.bigRowService.insert(creds, model)
    case model: models.b.BottomRow => TestServices.bottomRowService.insert(creds, model)
    case model: models.size.SmallRow => TestServices.smallRowService.insert(creds, model)
    case model: models.t.TopRow => TestServices.topRowService.insert(creds, model)
    case model => throw new IllegalStateException(s"Unable to insert unhandled model [$model]")
  }

  def remove(m: DataFieldModel) = m match {
    case model: models.size.BigRow => TestServices.bigRowService.remove(creds, model.id)
    case model: models.b.BottomRow => TestServices.bottomRowService.remove(creds, model.id)
    case model: models.size.SmallRow => TestServices.smallRowService.remove(creds, model.id)
    case model: models.t.TopRow => TestServices.topRowService.remove(creds, model.id)
    case model => throw new IllegalStateException(s"Unable to remove unhandled model [$model]")
  }
}
