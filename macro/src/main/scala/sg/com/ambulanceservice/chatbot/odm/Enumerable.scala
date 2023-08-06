package sg.com.ambulanceservice.chatbot.odm

// Unfortunately, I can't make the type of the serializedForm
// generic because of type erasure
trait Enumerable {
  def serializedForm: String
}
