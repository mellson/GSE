package dk.itu.spcl.server

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class PresenceServiceSpec extends Specification with Specs2RouteTest with PresenceService {
  def actorRefFactory = system
  lazy val randomSensor = Sensor(TestHelper.randomString(10), TestHelper.randomString(10))

  "PresenceService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> route ~> check {
        responseAs[String] must contain("Hej Hej")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> route ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(route) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }

    import SensorJsonSupport._
    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Post("/sensor", randomSensor) ~> route ~> check {
        responseAs[String].contains(randomSensor.name)
      }
    }
  }
}
