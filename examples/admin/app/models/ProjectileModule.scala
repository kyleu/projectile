package models

import com.kyleu.projectile.models.AdminModule
import com.kyleu.projectile.models.user.Role
import models.template.UserMenu
import util.Version

class ProjectileModule extends AdminModule(projectName = Version.projectName, allowSignup = true, initialRole = Role.Admin, menuProvider = UserMenu)
