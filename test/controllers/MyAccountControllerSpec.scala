package controllers

import java.util.concurrent.TimeUnit

import controllers.requests.user.GetUserResponse
import org.scalatest._
import play.api.Play
import play.api.libs.json.{JsArray, JsUndefined, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import tests.{ProjectTestUtils, JasmineSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration

/**
 * Created by liviuignat on 10/04/15.
 */
class MyAccountControllerSpec extends JasmineSpec with BeforeAndAfter with BeforeAndAfterAll with ShouldMatchers {
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

      describe("When wanting to insert a second user") {
        var response: Option[Future[Result]] = null
        var result: Result = null
        beforeEach {
          val request = FakeRequest.apply("POST", "/api/auth/create")
            .withJsonBody(Json.obj(
            "email" -> "liviu.test@ignat.email",
            "password" -> "test123",
            "firstName" -> "Liviu Test",
            "lastName" -> "Ignat"))
          response = route(request)

          result = Await.result(response.get, timeout)
        }

        describe("When getting the token") {
          var response: Option[Future[Result]] = null
          var result: Result = null
          var accessToken: String = null

          beforeEach {
            val request = FakeRequest.apply("POST", "/api/auth/access_token")
              .withJsonBody(Json.obj(
              "grant_type" -> "password",
              "client_id" -> "DefaultClient",
              "client_secret" -> "DefatultClientSecret",
              "scope" -> "offline_access",
              "username" -> "liviu.test@ignat.email",
              "password" -> "cc03e747a6afbbcbf8be7668acfebee5"))

            response = route(request)
            result = Await.result(response.get, timeout)
            val json: JsValue = contentAsJson(response.get)
            accessToken = json.\("access_token").as[String]
          }

          describe("When update my account") {
            var response: Option[Future[Result]] = null
            var result: Result = null

            beforeEach {
              val request = FakeRequest.apply("PUT", s"/api/user/me")
                .withHeaders("Authorization" -> s"Bearer $accessToken")
                .withJsonBody(Json.obj(
                "firstName" -> "Liviu Updated",
                "lastName" -> "Ignat Updated"))
              response = route(request)
              result = Await.result(response.get, timeout)
            }

            it("Should work with success") {
              response.isDefined should equal(true)
              result.header.status should equal(OK)
            }

            describe("When getting my account") {
              var response: Option[Future[Result]] = null
              var result: Result = null
              beforeEach {
                val uri = s"/api/user/me"
                val request = FakeRequest.apply("GET", uri)
                  .withHeaders("Authorization" -> s"Bearer $accessToken")
                response = route(request)
                result = Await.result(response.get, timeout)
              }

              it("Should have the updated first name") {
                val json: JsValue = contentAsJson(response.get)
                val firstName: String = json.\("firstName").as[String]
                firstName should equal("Liviu Updated")
              }

              it("Should have the updated last name") {
                val json: JsValue = contentAsJson(response.get)
                val firstName: String = json.\("lastName").as[String]
                firstName should equal("Ignat Updated")
              }
            }
          }
        }
      }
    }
  }
}
