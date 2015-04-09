package controllers.requests

import business.models.User
import controllers.requests.auth.CreateUserRequest
import controllers.requests.user.{GetUserResponse, UpdateMyAccountRequest}
import reactivemongo.bson.BSONObjectID

/**
 * Created by liviuignat on 22/03/15.
 */
object Mappings {
  implicit def createUserRequestToUser(req: CreateUserRequest) =
    User(_id = BSONObjectID.generate.stringify,
      email = req.email,
      password = req.password,
      firstName = req.firstName,
      lastName = req.lastName)


  implicit def userToGetUserResponse(u: User) =
    GetUserResponse(id = u._id,
      firstName = u.firstName,
      lastName = u.lastName,
      isActive = u.isActive)
}
