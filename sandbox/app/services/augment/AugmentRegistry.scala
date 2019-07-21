package services.augment

import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.services.augment.AugmentService
import com.kyleu.projectile.util.tracing.TraceData
import play.twirl.api.Html

import scala.concurrent.Future

@javax.inject.Singleton
class AugmentRegistry @javax.inject.Inject() () {
  val creds = Credentials.system
  implicit val td: TraceData = TraceData.noop

  AugmentService.lists.register[SystemUser]((models, args, cfg, td) => {
    val additional = models.map(m => m -> Some(Html(s"<td>${m.role}</td>"))).toMap
    Future.successful(Some(Html("<td>role</td>")) -> additional)
  })
}
