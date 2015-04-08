package test.controllers

import java.util.concurrent.TimeUnit

import business.models._
import business.models.JsonFormats._
import org.scalatest._
import play.api.Play
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.AnyContentAsJson
import play.api.test.Helpers._
import play.api.test._
import tests._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.FiniteDuration

class AuthControllerSpec extends JasmineSpec with BeforeAndAfter with BeforeAndAfterAll with ShouldMatchers {
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)
  var firstUserId : String = "";

  override def beforeAll() = {
    Play.start(ProjectTestUtils.fakeApplication)
  }

  override def afterAll() = {
    Play.stop
  }

  describe("UsersControllerSpec") {
    beforeEach {
      ProjectTestUtils.dropDb()
    }

    describe("When wanting to insert a first user") {
      var response: Option[Future[Result]] = null
      var result: Result = null
      beforeEach {
        val request = FakeRequest.apply("POST", "/api/auth/create")
          .withJsonBody(Json.obj(
          "email" -> "liviu@ignat.email",
          "password" -> "test123",
          "firstName" -> "Liviu",
          "lastName" -> "Ignat"))
        response = route(request)

        result = Await.result(response.get, timeout)
        firstUserId = result.header.headers.get("Location").get
      }

      it("Should be able to the request with success") {
        response.isDefined should equal(true)
      }

      it("Should have the Status Header Created") {
        result.header.status should equal(CREATED)
      }

      it("Should have the Location header defined") {
        assert(result.header.headers.contains("Location") == true)
      }

      it("Should have the Location header with length 24") {
        result.header.headers.get("Location").get.length should equal(24)
      }

      describe("When trying to get the token with correct credentials") {
        var response: Option[Future[Result]] = null
        var result: Result = null
        var accessToken: String = null
        var refreshToken: String = null

        beforeEach {
          val request = FakeRequest.apply("POST", "/api/auth/access_token")
            .withJsonBody(Json.obj(
            "grant_type" -> "password",
            "client_id" -> "DefaultClient",
            "client_secret" -> "DefatultClientSecret",
            "scope" -> "offline_access",
            "username" -> "liviu@ignat.email",
            "password" -> "cc03e747a6afbbcbf8be7668acfebee5"))

          response = route(request)
          result = Await.result(response.get, timeout)
          val json: JsValue = contentAsJson(response.get)
          accessToken = json.\("access_token").as[String]
          refreshToken = json.\("refresh_token").as[String]
        }

        it("Should be able to the request with success") {
          response.isDefined should equal(true)
          result.header.status should equal(OK)
        }

        it("Should get access_token property") {
          val json: JsValue = contentAsJson(response.get)
          assert(json.\("access_token").getClass != (new JsUndefined("")).getClass)
        }

        it("Should get expires_in property") {
          val json: JsValue = contentAsJson(response.get)
          assert(json.\("access_token").getClass != (new JsUndefined("")).getClass)
        }

        it("Should get refresh_token property") {
          val json: JsValue = contentAsJson(response.get)
          assert(json.\("refresh_token").getClass != (new JsUndefined("")).getClass)
        }

        it("Should get token_type property") {
          val json: JsValue = contentAsJson(response.get)
          assert(json.\("token_type").getClass != (new JsUndefined("")).getClass)
        }

        describe("When refreshing the token") {
          var response: Option[Future[Result]] = null
          var result: Result = null

          beforeEach {
            val request = FakeRequest.apply("POST", "/api/auth/access_token")
              .withJsonBody(Json.obj(
              "grant_type" -> "refresh_token",
              "client_id" -> "DefaultClient",
              "client_secret" -> "DefatultClientSecret",
              "refresh_token" -> refreshToken
              ))

            response = route(request)
            result = Await.result(response.get, timeout)
          }

          it("Should be able to the request with success") {
            response.isDefined should equal(true)
            result.header.status should equal(OK)
          }

          it("Should get new access_token property") {
            val json: JsValue = contentAsJson(response.get)
            json.\("access_token").as[String] should not equal(accessToken)
          }

          it("Should get same refresh_token property") {
            val json: JsValue = contentAsJson(response.get)
            json.\("refresh_token").as[String] should equal(refreshToken)
          }
        }

        describe("When getting first user") {
          var response: Option[Future[Result]] = null
          var result: Result = null
          var getUserResponse: JsValue = null;

          beforeEach {
            val uri = s"/api/user/$firstUserId"
            val request = FakeRequest.apply("GET", uri)
            response = route(request)

            result = Await.result(response.get, timeout)
            getUserResponse = contentAsJson(response.get)
          }

          it("Should have a defined response") {
            response.isDefined should equal(true)
          }

          it("Should be able make the request with success") {
            result.header.status should equal(OK)
          }

          describe("When creating a new user with the same email") {
            var response: Option[Future[Result]] = null
            var result: Result = null
            beforeEach {
              val request = FakeRequest.apply("POST", "/api/auth/create")
                .withJsonBody(Json.obj(
                "email" -> "liviu@ignat.email",
                "password" -> "anothertest123",
                "firstName" -> "Liviu",
                "lastName" -> "Ignat"))
              response = route(request)
              result = Await.result(response.get, timeout)
            }

            it("Should fail the request") {
              response.isDefined should equal(true)
              result.header.status should equal(BAD_REQUEST)
            }

            it("Should not have response message undefined") {
              val json: JsValue = contentAsJson(response.get)
              val password = json.\("message")

              assert(password.getClass != (new JsUndefined("")).getClass)
            }

            it("Should have message equal to 'User already exists'") {
              val json: JsValue = contentAsJson(response.get)
              val password = json.\("message")

              password.as[String] should equal("User already exists")
            }
          }

          describe("When resetting password for first user") {
            var response: Option[Future[Result]] = null
            var result: Result = null

            beforeEach {
              val request = FakeRequest.apply("POST", "/api/auth/resetpassword")
                .withJsonBody(Json.obj("email" -> "liviu@ignat.email"))

              response = route(request)
              result = Await.result(response.get, timeout)
            }

            it("Should be able make the request with success") {
              response.isDefined should equal(true)
              result.header.status should equal(OK)
            }
          }
        }
      }

      describe("When trying to get the token with false credentials") {
        var response: Option[Future[Result]] = null
        var result: Result = null

        beforeEach {
          val request = FakeRequest.apply("POST", "/api/auth/access_token")
            .withJsonBody(Json.obj(
            "grant_type" -> "password",
            "client_id" -> "DefaultClient",
            "client_secret" -> "DefatultClientSecret",
            "scope" -> "offline_access",
            "username" -> "liviu@ignat.email",
            "password" -> "cc03e747a6afbbcbf8be7668acfebee5_not_correct"))

          response = route(request)
          result = Await.result(response.get, timeout)
        }

        it("Should receive a not authorized 401 response") {
          response.isDefined should equal(true)
          result.header.status should equal(UNAUTHORIZED)
        }
      }


      describe("When creating a second user") {
        var response: Option[Future[Result]] = null
        var result: Result = null
        beforeEach {
          val request = FakeRequest.apply("POST", "/api/auth/create")
            .withJsonBody(Json.obj(
            "email" -> "liviu.ignat@someotheremail.com",
            "password" -> "someotheremailtest123",
            "firstName" -> "Liviu Marius",
            "lastName" -> "Ignat Someother"))
          response = route(request)
          result = Await.result(response.get, timeout)
        }

        it("Should be able make the request with success") {
          response.isDefined should equal(true)
          result.header.status should equal(CREATED)
        }
      }
    }
  }


}
