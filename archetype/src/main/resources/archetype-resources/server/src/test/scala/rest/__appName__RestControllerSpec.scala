
package ${package}.rest

import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers}
import org.scalatest.junit.JUnitRunner
import com.twitter.inject.server.FeatureTest
import com.twitter.finagle.http.Status._
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.EmbeddedHttpServer
import ${package}.${appName}Manager

class FakeServer(controller : ${appName}RestController) extends HttpServer {
  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add(controller)
  }
}

@RunWith(classOf[JUnitRunner])
class ${appName}RestControllerSpec extends FeatureTest with BeforeAndAfter with Matchers {

  val manager = new ${appName}Manager
  val controller = new ${appName}RestController(manager)

  override val server = new EmbeddedHttpServer(new FakeServer(controller))

  test(" /ping will return pong ") {
    server.httpGet(
          path = "/ping",
          andExpect = Ok,
          withBody = "pong\n")
  }
}
