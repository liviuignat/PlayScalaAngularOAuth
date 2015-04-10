package controllers

import javax.inject.Inject

import business.models.{PhoneType, Phone, Gender, User}
import business.repositories.IUserRepository
import business.services.IStringEncriptionService
import controllers.requests.user.{GetMyAccountResponse, UpdateMyAccountRequest}
import org.slf4j.{LoggerFactory, Logger}
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController

import scala.concurrent.Future

class MyAccountController @Inject() (
  encriptionService: IStringEncriptionService,
  userRepository: IUserRepository,
  authDataHandlerFactory: IOAuthDataHandlerFactory) extends Controller with MongoController with MyOAuth2Provider {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[UsersController])

  import controllers.requests.Mappings._
  import controllers.requests.JsonFormats._
  implicit val UpdateMyAccountRequestFormat = Json.format[UpdateMyAccountRequest]
  implicit val GetMyAccountResponseFormat = Json.format[GetMyAccountResponse]
  implicit def userToMyAccountResponse(user: User) = GetMyAccountResponse(
    id = user._id,
    email = user.email,
    firstName = user.firstName,
    lastName = user.lastName,
    zipCode = user.zipCode,
    profilePhoto = user.profilePhoto,
    description = user.description,
    gender = user.gender.id,
    birthDate = user.birthDate,
    createdDate = user.createdDate,
    phone = user.phone
  )

  def getMyAccount() = Action.async { implicit req =>
    authorize(authDataHandlerFactory.getInstance()) { authInfo =>
      userRepository.getById(authInfo.user._id).map {
        case Some(user) => {
          val response: GetMyAccountResponse = user
          val responseJson = Json.toJson(response)
          Ok(responseJson)
        }
        case None => NotFound(Json.obj("message" -> "No such item"))
      }
    }
  }

  def updateMyAccount() = Action.async(parse.json) { implicit req =>
    authorize(authDataHandlerFactory.getInstance()) { authInfo =>
      req.body.validate[UpdateMyAccountRequest].map {
        updateUserRequest => {
          val user: User = authInfo.user
          user.email = updateUserRequest.email.getOrElse(user.email)
          user.firstName = updateUserRequest.firstName.getOrElse(user.firstName)
          user.lastName = updateUserRequest.lastName.getOrElse(user.lastName)
          user.zipCode = updateUserRequest.zipCode
          user.profilePhoto = updateUserRequest.profilePhoto
          user.description = updateUserRequest.description
          user.gender = Gender(updateUserRequest.gender.getOrElse(0))
          user.birthDate = updateUserRequest.birthDate
          user.phone = updateUserRequest.phone

          userRepository.update(user).map({
            case lastError if lastError.ok() => Ok("")
            case lastError if !lastError.ok() => InternalServerError(Json.obj("message" -> "Internal server error"))
          })
        }
      }.getOrElse(Future.successful(BadRequest(Json.obj("message" -> "Invalid json"))))
    }
  }

  def disableAccount() = Action.async { implicit req =>
    authorize(authDataHandlerFactory.getInstance()) { authInfo =>
      userRepository.delete(authInfo.user._id).map({
        case lastError if lastError.ok() => Ok("")
        case lastError if !lastError.ok() => InternalServerError(Json.obj("message" -> "Internal server error"))
      })
    }
  }

}
