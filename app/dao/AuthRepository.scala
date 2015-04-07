package dao

import scala.concurrent.ExecutionContext.Implicits._

import business.models._
import business.repositories._
import common.LastError

import scala.concurrent.Future

class AuthRepository extends IAuthRepository {
  override def getClients(): Future[List[AuthClient]] = {
    Future.successful(List(
      AuthClient("1", name = Some("DefaultClient"), clientSecret = Some("DefatultClientSecret"))
    ))
  }

  override def getClientById(id: String): Future[Option[AuthClient]] = {
    getClients().flatMap(list => Future.successful(Some(list(0))))
  }

  override def getAuthTokenByUserAndClient(userId: String, clientId: String): Future[Option[AuthAccessToken]] = ???

  override def getAccessToken(token: String): Future[Option[AuthAccessToken]] = ???

  override def insertAuthToken(authAccessToken: AuthAccessToken): Future[LastError] = ???

  override def deleteAuthTokensByUserAndClient(userId: String, clientId: String): Future[LastError] = ???

  override def getRefreshToken(token: String): Future[Option[AuthAccessToken]] = ???

  override def getAuthCode(code: String): Future[Option[AuthCode]] = ???
}
