package business.repositories

import business.models._
import common.LastError

import scala.concurrent.Future

/**
 * Created by liviuignat on 05/04/15.
 */
trait IAuthRepository {
  def getClients(): Future[List[AuthClient]]

  def getClientById(id: String): Future[Option[AuthClient]]

  def getAuthTokenByUserAndClient(userId: String, clientId: String): Future[Option[AuthAccessToken]]

  def getAccessToken(token: String): Future[Option[AuthAccessToken]]

  def getRefreshToken(token: String): Future[Option[AuthAccessToken]]

  def getAuthCode(code: String): Future[Option[AuthCode]]

  def insertAuthToken(authAccessToken: AuthAccessToken): Future[LastError]

  def updateAuthToken(authAccessToken: AuthAccessToken): Future[LastError]

  def deleteAuthTokensByUserAndClient(userId: String, clientId: String): Future[LastError]
}
