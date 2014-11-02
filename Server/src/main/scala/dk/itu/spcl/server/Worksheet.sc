

//val t = "2014-11-01T16:26:20.8422459+01:00"
//val d = DateTime.parse(t)
//d.dayOfWeek().get()
//d.hourOfDay().get()
//d.minuteOfDay().get()
//
//import spray.json._
//import DefaultJsonProtocol._
//val json = """{"SensorName":"MouseSensor","Time":"2014-11-01T16:26:04.8345115+01:00","Value":"X:1011,Y:419"}"""
//for (o <- JsonParser(json))
//  println(1)
//import dk.itu.spcl.server.UserStatusJsonSupport._



import spray.json._
import DefaultJsonProtocol._
val jsonAst = List("1", "2", "3").toJson.compactPrint

//val u1 = UserStatus("Frank", true, 65)
//val u2 = UserStatus("Per", false, 15)
//val users = List(u1, u2)
//
//users.toJson


