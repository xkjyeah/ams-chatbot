package sg.com.ambulanceservice.chatbot.odm

/**
 * A trait to be extended by **sealed traits** only.
 * Which will allow all subclasses of the sealed trait to be
 * serialized in such a way that can preserve the type knowledge
 *
 * Subclasses must have the annotation @{annotations.serializedTypeName}
 */
trait AutoConvert {

}
