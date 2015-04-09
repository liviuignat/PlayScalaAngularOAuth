package dao

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits._
import play.api.Play.current

import business.models._
import business.repositories._
import common._

import scala.concurrent.Future

class AuthRepository extends IAuthRepository {
  import play.api.libs.json.Json
  implicit val authClientFormat = Json.format[AuthClient]
  implicit val authAccessTokenFormat = Json.format[AuthAccessToken]
  implicit val authCodeFormat = Json.format[AuthCode]

  private def authTokensCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("auth_tokens")

  /*
  * Decided to hard-code the clients for now
  */
  override def getClients(): Future[List[AuthClient]] = {
    Future.successful(List(
      AuthClient("1", name = Some("DefaultClient"), clientSecret = Some("DefatultClientSecret"))
    ))
  }

  override def getClientById(id: String): Future[Option[AuthClient]] = {
    getClients().flatMap(list => Future.successful(Some(list(0))))
  }

  override def getAuthTokenByUserAndClient(userId: String, clientId: String): Future[Option[AuthAccessToken]] = {
    val selector = Json.obj("userId" -> userId,"clientId" -> clientId)
    authTokensCollection.find(selector).one[AuthAccessToken]
  }

  override def getAccessToken(accessToken: String): Future[Option[AuthAccessToken]] = {
    val selector = Json.obj("accessToken" -> accessToken)
    authTokensCollection.find(selector).one[AuthAccessToken]
  }

  override def insertAuthToken(authAccessToken: AuthAccessToken): Future[LastError] = {
    authTokensCollection.insert(authAccessToken).map {
      case ok if ok.ok => NoError()
      case error => Error(Some(new RuntimeException(error.message)))
    }
  }

  override def updateAuthToken(authAccessToken: AuthAccessToken): Future[LastError] = ???

  override def deleteAuthTokensByUserAndClient(userId: String, clientId: String): Future[LastError] = {
    val selector = Json.obj("userId" -> userId,"clientId" -> clientId)
    authTokensCollection.remove(selector).map {
      case ok if ok.ok => NoError()
      case error => Error(Some(new RuntimeException(error.message)))
    }
  }

  override def getRefreshToken(refreshToken: String): Future[Option[AuthAccessToken]] = {
    val selector = Json.obj("refreshToken" -> refreshToken)
    authTokensCollection.find(selector).one[AuthAccessToken]
  }

  override def getAuthCode(code: String): Future[Option[AuthCode]] = ???
}
