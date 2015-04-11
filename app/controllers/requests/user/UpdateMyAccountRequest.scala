package controllers.requests.user

import java.util.Date

case class UpdateMyAccountRequest(
  var email: Option[String],
  var firstName: Option[String],
  var lastName: Option[String],
  var zipCode: Option[String],
  var profilePhoto: Option[String],
  var headline: Option[String],
  var description: Option[String],
  var birthDate: Option[Date],
  var gender: Option[Int],
  var phone: Option[PhoneResponse])
