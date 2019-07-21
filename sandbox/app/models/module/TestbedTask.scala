package models.module

import com.kyleu.projectile.models.sandbox.SandboxTask
import com.kyleu.projectile.util.tracing.TraceData

object TestbedTask extends SandboxTask("testbed", "Testbed", "Just new boot goofin'") {
  override def call(cfg: SandboxTask.Config)(implicit trace: TraceData) = {
    ???
  }
}
