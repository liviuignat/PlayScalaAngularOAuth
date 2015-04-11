package business.models

import java.util.Date

object Gender extends Enumeration {
  type Gender = Value
  val Empty = Value(0)
  val Male = Value(1)
  val Female = Value(2)
}

object PhoneType extends Enumeration {
  type PhoneType = Value
  val Empty = Value(0)
  val Mobile = Value(1)
  val Home = Value(2)
  val Work = Value(3)
}

import business.models.Gender._
import business.models.PhoneType._

case class Phone(
  var phoneNumber: String,
  var phoneType: PhoneType = PhoneType(0)
)

case class Person(
  _id: String,
  firstName: String,
  lastName: String,
  profilePhoto: String
)

case class User(
  var _id: String,
  var email: String,
  var password: String,
  var firstName: String,
  var lastName: String,
  var zipCode: Option[String] = None,
  var profilePhoto: Option[String] = None,

  var headline: Option[String] = None,
  var description: Option[String] = None,
  var birthDate: Option[Date] = None,
  var gender: Option[Gender] = None,
  var phone: Option[Phone] = None,

  var createdDate: Date = new Date(),
  var isActive: Boolean = true)



