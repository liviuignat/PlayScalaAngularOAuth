package business.models

case class AuthCode(
   authorizationCode: String,
   userId: String,
   redirectUri: Option[String],
   createdAt: java.util.Date,
   scope: Option[String],
   clientId: String,
   expiresIn: Int)

