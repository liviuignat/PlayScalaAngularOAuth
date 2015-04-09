
import com.tzavellas.sse.guice.ScalaModule
import business.repositories._
import business.services._
import controllers._
import dao._

class Module extends ScalaModule {
  def configure() {
    bind[IUserRepository].to[UserRepository]
    bind[IAuthRepository].to[AuthRepository]

    bind[IOAuthDataHandlerFactory].to[OAuthDataHandlerFactory]
    bind[IStringEncriptionService].to[StringEncriptionService]
    bind[IRandomStringService].to[RandomStringService]
    bind[IEmailService].to[EmailService]
  }
}
