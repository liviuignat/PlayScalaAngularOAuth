package business.models

/**
 * Created by liviuignat on 05/04/15.
 */
case class AuthClient (
   _id: String,
   name: Option[String],
   clientSecret: Option[String],
   trustedClient: Boolean = true,
   isActive: Boolean = true)
