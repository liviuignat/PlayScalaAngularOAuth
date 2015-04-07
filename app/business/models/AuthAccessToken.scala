package business.models

case class AuthAccessToken(
  clientId: String,
  accessToken: String,
  refreshToken: Option[String],
  userId: String,
  scope: Option[String],
  expiresIn: Int,
  createdAt: java.util.Date)


