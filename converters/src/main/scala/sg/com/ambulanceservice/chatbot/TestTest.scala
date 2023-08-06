package sg.com.ambulanceservice.chatbot

import sg.com.ambulanceservice.chatbot.odm.{Converters, Enumerable}

package object test {
  sealed trait Bla extends Enumerable

  case object First extends Bla {
    val serializedForm = "First"
  }

  case object Second extends Bla {
    val serializedForm = "Second"
  }
}


object TestTest extends scala.App {

  import Converters._

  val converter = Converters.convertEnumerable[test.Bla]

  val f = converter.serialize(test.First)
  val g = converter.deserialize(f)

  println((f, g, test.First == g))
}
