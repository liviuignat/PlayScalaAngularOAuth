package dao

import javax.inject.Inject

import business.models.Gender.Gender
import business.models.PhoneType
import business.models.PhoneType._
import business.models._
import business.repositories._
import common._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONRegex
import play.modules.reactivemongo.json.BSONFormats._

import scala.concurrent.Future
import play.api.Play.current

import scala.util.matching.Regex

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsNumber(s) => {
        try {
          enum.values.find(v => v.id == s) match {
            case Some(value) => JsSuccess(value)
            case _ => enum.values.find(v => v == 0) match {
              case Some(value) => JsSuccess(value)
              case _ => JsError(s"Enumeration: '${enum.getClass}', has no default value")
            }
          }

        } catch {
          case _: NoSuchElementException =>
            JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'")
        }
      }
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsNumber(v.id)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}

class UserRepository @Inject() () extends IUserRepository {

  implicit val PhoneTypeFormat = EnumJson.enumFormat(PhoneType)
  implicit val GenderFormat = EnumJson.enumFormat(Gender)
  implicit val PhoneFormat = Json.format[Phone]
  implicit val PersonFormat = Json.format[Person]
  implicit val UserFormat = Json.format[User]

  private def collection = ReactiveMongoPlugin.db
    .collection[JSONCollection]("app_users")
/*
  Causes some problems on reactive mongo - for sure some permissions

  collection.indexesManager.ensure(Index(List(
    "email" -> IndexType.Ascending), unique = false))
  collection.indexesManager.ensure(Index(List(
    "firstName" -> IndexType.Ascending), unique = false))
  collection.indexesManager.ensure(Index(List(
    "lastName" -> IndexType.Ascending), unique = false))
*/

  override def getById(id: String): Future[Option[User]] = {
    collection
      .find(Json.obj("_id" -> id,"isActive" -> true)).one[User]
  }

  override def getByEmail(email: String): Future[Option[User]] = {
    val selector =  Json.obj("email" -> email, "isActive" -> true)
    collection.find(selector).one[User]
  }

  override def getByEmailAndPassword(email: String, password: String): Future[Option[User]] = {
    val regex = Json.obj("$regex" -> (".*" + email + ".*"), "$options" -> "-i")
    val selector =  Json.obj(
      "email" -> regex,
      "password" -> password,
      "isActive" -> true)
    collection.find(selector).one[User]
  }

  override def getAll(query: Option[String]): Future[List[User]] = {

    val selector = query match {
      case None => Json.obj()
      case Some("") => Json.obj()
      case Some(query) => {
        val regex = Json.obj("$regex" -> (".*" + query + ".*"), "$options" -> "-i")
        Json.obj("$or" -> Seq(
          Json.obj("firstName" -> regex),
          Json.obj("lastName" -> regex)
        ))
      }
    };

    val cursor: Cursor[User] = collection.find(selector).cursor[User]

    cursor.collect[List]()
  }

  override def insert(user: User): Future[LastError] = {
    collection.insert(user).map {
      case ok if ok.ok => NoError()
      case error => Error(Some(new RuntimeException(error.message)))
    }
  }

  override def update(user: User): Future[LastError] = {
    val selector = Json.obj("_id" -> user._id)

    /*
    Still deciding if to send Json or the full object. Should the full object be replaced ... maybe yes
    val jsonToUpdate = Json.obj(
      "firstName" -> user.firstName,
      "lastName" -> user.lastName)
    */

    val json = Json.toJson(user).as[JsObject] - "_id"

    val modifier = Json.obj("$set" -> json)

    collection.update(selector, modifier, multi = true).map {
      case ok if ok.ok =>
        NoError()
      case error => Error(Some(new RuntimeException(error.message)))
    }
  }

  override def resetPassword(email: String, newPassword: String): Future[LastError] = {
    val selector = Json.obj("email" -> email)
    val modifier = Json.obj("$set" -> Json.obj("password" -> newPassword))

    collection.update(selector, modifier, multi = true).map {
      case ok if ok.ok =>
        NoError()
      case error => Error(Some(new RuntimeException(error.message)))
    }
  }

  override def delete(id: String): Future[LastError] = {
    val selector = Json.obj("_id" -> id)

    collection.remove(selector).map {
      case ok if ok.ok =>
        NoError()
      case error => Error(Some(new RuntimeException(error.message)))
    }
  }
}
