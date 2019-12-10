package services.augment

import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

@javax.inject.Singleton
class AugmentRegistry @javax.inject.Inject() () {
  val creds = Credentials.system
  implicit val td: TraceData = TraceData.noop

  /*
  AugmentService.listHeaders.register(classOf[SystemUser], (cls, args, cfg, td) => {
    Future.successful(Some(Html("<div>AUGMENT!</div>")))
  })

  AugmentService.lists.register[SystemUser]((models, args, cfg, td) => {
    val additional = models.map(m => m -> Some(Html(s"<td>${m.role}</td>"))).toMap
    Future.successful(Some(Html("<th>Role</th>")) -> additional)
  })
  */
}
