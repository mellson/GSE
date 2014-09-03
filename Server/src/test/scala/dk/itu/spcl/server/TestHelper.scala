package dk.itu.spcl.server

object TestHelper {
  def randomString(length: Int) = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to length) {
      sb.append(r.nextPrintableChar())
    }
    sb.toString()
  }
}
