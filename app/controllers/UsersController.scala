package controllers

import javax.inject._
import controllers.requests.user.{GetUserResponse, UpdateMyAccountRequest}
import org.slf4j._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.MongoController

import scala.concurrent.Future

import business.repositories._
import business.services._
import business.models._

import requests._
import requests.JsonFormats._
import requests.Mappings._

@Singleton
class UsersController @Inject() (
  encriptionService: IStringEncriptionService,
  userRepository: IUserRepository,
  authDataHandlerFactory: IOAuthDataHandlerFactory) extends Controller with MongoController with MyOAuth2Provider {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[UsersController])

  def getUserById(id: String) = Action.async { implicit req =>
    authorize(authDataHandlerFactory.getInstance()) { authInfo =>
      userRepository.getById(id).map {
        case Some(user) => {
          val response: GetUserResponse = user
          val responseJson = Json.toJson(response)
          Ok(responseJson)
        }
        case None => NotFound(Json.obj("message" -> "No such item"))
      }
    }
  }

  def getUserSearch() = Action.async(parse.anyContent) { implicit req =>
    authorize(authDataHandlerFactory.getInstance()) { authInfo =>
      val query = req.getQueryString("q");

      userRepository.getAll(query).map({
        users => {
          val response: List[GetUserResponse] = users.map(user => {
            val userResponse: GetUserResponse = user
            userResponse
          })
          Ok(Json.toJson(response))
        }
      })
    }
  }
}
