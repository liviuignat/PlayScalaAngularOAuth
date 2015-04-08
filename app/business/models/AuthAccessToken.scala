package business.models

case class AuthAccessToken(
  clientId: String,
  userId: String,
  accessToken: String,
  refreshToken: Option[String],
  scope: Option[String],
  expiresIn: Int,
  createdAt: java.util.Date)


