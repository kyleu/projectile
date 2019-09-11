/* Generated File */
import com.kyleu.projectile.models.result.data.DataFieldModel
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

object TestModelSupport {
  private[this] val creds = Credentials.noop
  private[this] implicit val td: TraceData = TraceData.noop

  def insert(m: DataFieldModel) = m match {
    case model: models.BottomRow => TestServices.bottomRowService.insert(creds, model)
    case model: models.TopRow => TestServices.topRowService.insert(creds, model)
    case model => throw new IllegalStateException(s"Unable to insert unhandled model [$model]")
  }

  def remove(m: DataFieldModel) = m match {
    case model: models.BottomRow => TestServices.bottomRowService.remove(creds, model.id)
    case model: models.TopRow => TestServices.topRowService.remove(creds, model.id)
    case model => throw new IllegalStateException(s"Unable to remove unhandled model [$model]")
  }
}
