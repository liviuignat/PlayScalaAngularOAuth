package controllers.requests.auth

/**
 * Created by liviuignat on 22/03/15.
 */
case class CreateUserRequest(
  email: String,
  password: String,
  firstName: String,
  lastName: String)
