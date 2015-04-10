package controllers.requests

import business.models.{PhoneType, Phone, User}
import controllers.requests.auth.CreateUserRequest
import controllers.requests.user.{PhoneResponse, GetUserResponse, UpdateMyAccountRequest}
import reactivemongo.bson.BSONObjectID

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

  implicit def phoneToPhoneResponse(p: Phone): PhoneResponse = PhoneResponse(p.phoneNumber, p.phoneType.id)
  implicit def phoneResponseToPhone(p: PhoneResponse): Phone = Phone(p.phoneNumber, PhoneType(p.phoneType))
  implicit def optionalPhoneToPhoneResponse(p: Option[Phone]): Option[PhoneResponse] = p match {
    case Some(phone) => Some(phone)
    case _ => None
  }
  implicit def optionalPhoneResponseToPhone(p: Option[PhoneResponse]): Option[Phone] = p match {
    case Some(phone) => Some(phone)
    case _ => None
  }
}
