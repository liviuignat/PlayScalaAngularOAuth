package controllers.requests.user

import java.util.Date

case class GetMyAccountResponse(
  id: String,
  email: String,
  firstName: String,
  lastName: String,

  zipCode: Option[String],
  profilePhoto: Option[String],
  description: Option[String],
  birthDate: Option[Date],
  gender: Int,
  phone: Option[PhoneResponse],
  createdDate: Date
)
