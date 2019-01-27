interp.load.ivy(
  coursier.Dependency(coursier.Module("com.kyleu", "projectile_2.12"), "1.1.2"),
  coursier.Dependency(coursier.Module("com.kyleu", "projectile_2.12"), "1.1.2", attributes = coursier.Attributes(classifier = "assets"))
)

@

import com.kyleu.projectile.web.util.PlayServerHelper._

startServer(30000)

println("Projectile started, press any key to exit")
System.in.read()

stopServer()
