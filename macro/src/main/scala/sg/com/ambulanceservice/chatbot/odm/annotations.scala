package sg.com.ambulanceservice.chatbot.odm

import scala.annotation.Annotation

object annotations {
  class serializedTypeName(val s: String) extends Annotation

  class defaultValue(val a: Any) extends Annotation
}
