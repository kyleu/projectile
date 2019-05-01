package com.kyleu.projectile.services.user

import com.kyleu.projectile.models.queries.auth.OAuth2InfoQueries
import com.kyleu.projectile.services.database.JdbcDatabase
import com.kyleu.projectile.util.tracing.OpenTracingService
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class OAuth2InfoService @javax.inject.Inject() (
    db: JdbcDatabase, tracingService: OpenTracingService
)(implicit ec: ExecutionContext) extends DelegableAuthInfoDAO[OAuth2Info] {

  override def find(loginInfo: LoginInfo) = tracingService.noopTrace("oauth2.find") { implicit td =>
    Future.successful(db.query(OAuth2InfoQueries.getByPrimaryKey(loginInfo.providerID, loginInfo.providerKey)))
  }

  override def add(loginInfo: LoginInfo, authInfo: OAuth2Info) = tracingService.noopTrace("oauth2.add") { implicit td =>
    db.executeF(OAuth2InfoQueries.CreateOAuth2Info(loginInfo, authInfo)).map(_ => authInfo)
  }

  override def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = tracingService.noopTrace("oauth2.update") { implicit td =>
    db.executeF(OAuth2InfoQueries.UpdateOAuth2Info(loginInfo, authInfo)).map(_ => authInfo)
  }

  override def save(loginInfo: LoginInfo, authInfo: OAuth2Info) = tracingService.noopTrace("oauth2.save") { implicit td =>
    db.executeF(OAuth2InfoQueries.UpdateOAuth2Info(loginInfo, authInfo))(td).flatMap { rowsAffected =>
      if (rowsAffected == 0) {
        db.executeF(OAuth2InfoQueries.CreateOAuth2Info(loginInfo, authInfo))(td).map(_ => authInfo)
      } else {
        Future.successful(authInfo)
      }
    }
  }

  override def remove(loginInfo: LoginInfo) = tracingService.topLevelTrace("oauth2.remove") { implicit td =>
    db.executeF(OAuth2InfoQueries.removeByPrimaryKey(loginInfo.providerID, loginInfo.providerKey)).map(_ => {})
  }
}
