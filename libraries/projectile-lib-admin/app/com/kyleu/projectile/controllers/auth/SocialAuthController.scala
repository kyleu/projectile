package com.kyleu.projectile.controllers.auth

import java.util.UUID

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.{AuthActions, AuthEnv, UserCredentials}
import com.kyleu.projectile.models.user.SystemUser
import com.kyleu.projectile.services.user.{SystemUserSearchService, SystemUserService}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider, SocialProviderRegistry}

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SocialAuthController @javax.inject.Inject() (
    override val app: Application,
    silhouette: Silhouette[AuthEnv],
    userService: SystemUserService,
    userSearchService: SystemUserSearchService,
    authInfoRepository: AuthInfoRepository,
    socialProviderRegistry: SocialProviderRegistry,
    actions: AuthActions
)(implicit ec: ExecutionContext) extends AuthController("socialAuth") {
  def authenticate(provider: String) = withoutSession("form") { implicit request => implicit td =>
    println("#############")
    println("#############")
    println("#############")
    println("#############")
    socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        val rsp = p.authenticate().flatMap {
          case Left(result) =>
            println("#############OK")
            println(result.session.data.toString())

            Future.successful(result)
          case Right(authInfo) => p.retrieveProfile(authInfo).flatMap { profile =>
            val li = LoginInfo(profile.loginInfo.providerID, profile.loginInfo.providerKey)

            val userF = userSearchService.getByLoginInfo(li).flatMap {
              case Some(u) => userService.updateUser(UserCredentials(u, request.remoteAddress), u)
              case None =>
                val dom = app.config.authWhitelistDomain
                log.info(s"Social auth called with [${profile.email.getOrElse("???")}] using whitelist domain [${dom.getOrElse("-")}]")
                if (dom.exists(x => !profile.email.exists(_.endsWith(x)))) {
                  throw new IllegalStateException(s"Email [${profile.email.getOrElse("-not provided-")}] must end with a whitelisted domain")
                }
                val username = profile.fullName.orElse(profile.firstName).orElse(profile.email).getOrElse(profile.loginInfo.providerKey)
                userService.findByUsername(UserCredentials.system, username).flatMap { existing =>
                  val newUser = SystemUser(
                    id = UUID.randomUUID,
                    username = if (existing.isDefined) { username + "-" + scala.util.Random.alphanumeric.take(4).mkString } else { username },
                    profile = profile.loginInfo,
                    role = actions.defaultRole
                  )
                  userService.insert(UserCredentials(newUser, request.remoteAddress), newUser)
                }
            }

            for {
              user <- userF
              _ <- authInfoRepository.save(profile.loginInfo, authInfo)
              authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
              value <- silhouette.env.authenticatorService.init(authenticator)
              result <- silhouette.env.authenticatorService.embed(value, Redirect(actions.indexUrl))
            } yield {
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              println("#############Create")
              println(result.session.data.toString())

              result
            }
          }
        }

        rsp.recover {
          case e: ProviderException =>
            log.error("Unexpected provider error", e)
            Redirect(actions.signinUrl).flashing("error" -> "Could not authenticate")
        }
      case _ => throw new IllegalStateException(s"Invalid provider [$provider]")
    }
  }
}
