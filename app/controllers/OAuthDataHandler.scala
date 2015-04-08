package controllers

import java.util.Date
import javax.inject.Inject

import business.models._
import business.services._
import business.repositories._
import play.api.libs.json.{JsValue, JsObject}
import play.api.mvc.Request

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scalaoauth2.provider._

trait MyOAuth2Provider extends OAuth2Provider {
  def getMyParam[A](request: Request[A]): Map[String, Seq[String]] = {
    request.body match {
      case body: play.api.mvc.AnyContentAsJson if body.asJson.isDefined => {
        body.asJson.get.as[JsObject].fields.toMap[String, JsValue].map{
          case (k, v) => k -> ArrayBuffer(v.as[String])
        }
      }
      case _ => getParam(request)
    }
  }

  override implicit def play2oauthRequest[A](request: Request[A]): AuthorizationRequest = {
    val param: Map[String, Seq[String]] = getMyParam(request)
    AuthorizationRequest(request.headers.toMap, param)
  }
}

trait IOAuthDataHandlerFactory {
  def getInstance(): OAuthDataHandler
}

class OAuthDataHandlerFactory @Inject() (
    authRepository: IAuthRepository,
    userRepository: IUserRepository,
    randomStringService: IRandomStringService,
    stringEncriptionService: IStringEncriptionService) extends IOAuthDataHandlerFactory {

  def getInstance(): OAuthDataHandler = {
    return new OAuthDataHandler(authRepository, userRepository, randomStringService, stringEncriptionService);
  }
}

class OAuthDataHandler(
    authRepository: IAuthRepository,
    userRepository: IUserRepository,
    randomStringService: IRandomStringService,
    stringEncriptionService: IStringEncriptionService) extends DataHandler[User] {

  implicit def mapToken(accessToken: AuthAccessToken): AccessToken =
    AccessToken(accessToken.accessToken, accessToken.refreshToken, accessToken.scope, Some(accessToken.expiresIn.toLong), accessToken.createdAt)

  implicit def mapToken(accessToken: Option[AuthAccessToken]): Option[AccessToken] = {
    accessToken match {
      case Some(token) => Some(token)
      case _ => None
    }
  }

  implicit def mapToken(accessToken: Future[Option[AuthAccessToken]]): Future[Option[AccessToken]] = {
    accessToken.map(token => token)
  }

  def validateClient(clientCredential: ClientCredential, grantType: String): Future[Boolean] =
    authRepository.getClientById(clientCredential.clientId).map {
      case None => false
      case _ => true
    }

  def findUser(username: String, password: String): Future[Option[User]] = {
    userRepository.getByEmailAndPassword(username, password)
  }

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
    val refreshToken = randomStringService.randomAlphaNumeric(32)
    val token = randomStringService.randomAlphaNumeric(32)
    val createdAt = new Date()
    val expiresIn = 3600

    val accessToken = AuthAccessToken(
      clientId = authInfo.clientId.get,
      accessToken = token,
      refreshToken = Some(refreshToken),
      expiresIn = expiresIn,
      scope = authInfo.scope,
      createdAt = createdAt,
      userId = authInfo.user._id
    )

    authRepository.deleteAuthTokensByUserAndClient(accessToken.userId, accessToken.clientId).flatMap {
      case lastError: common.Error => throw new RuntimeException()
      case lastError: common.NoError => {
        authRepository.insertAuthToken(accessToken).flatMap {
          case lastError: common.Error => throw new RuntimeException()
          case lastError: common.NoError => Future.successful(accessToken)
        }
      }
    }
  }


  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = {
    authRepository.getAuthTokenByUserAndClient(authInfo.user._id, authInfo.clientId.get)
  }

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = {
    val token = randomStringService.randomAlphaNumeric(32)
    val createdAt = new Date()
    val expiresIn = 3600
    val accessToken = AuthAccessToken(
      clientId = authInfo.clientId.get,
      accessToken = token,
      refreshToken = Some(refreshToken),
      expiresIn = expiresIn,
      scope = authInfo.scope,
      createdAt = createdAt,
      userId = authInfo.user._id
    )

    authRepository.deleteAuthTokensByUserAndClient(accessToken.userId, accessToken.clientId).flatMap {
      case lastError: common.Error => throw new RuntimeException()
      case lastError: common.NoError => {
        authRepository.insertAuthToken(accessToken).flatMap {
          case lastError: common.Error => throw new RuntimeException()
          case lastError: common.NoError => Future.successful(accessToken)
        }
      }
    }
  }

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = {
    authRepository.getAuthCode(code).flatMap {
      case None => Future.successful(None)
      case Some(code) => userRepository.getById(code.userId).flatMap {
        case None => Future.successful(None)
        case Some(user) => Future.successful(Some(AuthInfo(user, Some(code.clientId), code.scope, code.redirectUri)))
      }
    }
  }

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = {
    authRepository.getRefreshToken(refreshToken).flatMap {
      case None => Future.successful(None)
      case Some(code) => userRepository.getById(code.userId).flatMap {
        case None => Future.successful(None)
        case Some(user) => Future.successful(Some(AuthInfo(user, Some(code.clientId), code.scope, Some(""))))
      }
    }
  }

  def findClientUser(clientCredential: ClientCredential, scope: Option[String]): Future[Option[User]] = {
    Future.successful(None) // Not implemented yet
  }

  def findAccessToken(token: String): Future[Option[AccessToken]] = {
    authRepository.getAccessToken(token)
  }

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = {
    authRepository.getAccessToken(accessToken.token).flatMap {
      case None => Future.successful(None)
      case Some(code) => userRepository.getById(code.userId).flatMap {
        case None => Future.successful(None)
        case Some(user) => Future.successful(Some(AuthInfo(user, Some(code.clientId), code.scope, Some(""))))
      }
    }
  }
}
