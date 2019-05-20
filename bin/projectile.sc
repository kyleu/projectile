interp.load.ivy(
  coursier.Dependency(coursier.Module("com.kyleu", "projectile_2.12"), "1.9.3"),
  coursier.Dependency(coursier.Module("com.kyleu", "projectile_2.12"), "1.9.3", attributes = coursier.Attributes(classifier = "assets"))
)

@

import com.kyleu.projectile.models.web.PlayServerHelper._

startServer(20000)

println("Projectile started, press any key to exit")
System.in.read()

stopServer()
