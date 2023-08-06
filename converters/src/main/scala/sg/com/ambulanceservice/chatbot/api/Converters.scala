package sg.com.ambulanceservice.chatbot.api

import sg.com.ambulanceservice.chatbot.odm
import scala.language.experimental.macros

object Converters {

  import upickle.default._

  // Additional converter types that are not supported out-of-the-box by uPickle
  implicit val convertBigDecimal: ReadWriter[BigDecimal] = ReadWriter.join(
    implicitly[Reader[String]],
    implicitly[Writer[String]]
  )
    .bimap(
      (bd: BigDecimal) => bd.toString,
      (s: String) => BigDecimal(s)
    )

  // Additional converter types that are not supported out-of-the-box by uPickle
  implicit def writerEnum[T <: odm.Enumerable]: Writer[T] = implicitly[Writer[String]]
    .comap[T](_.serializedForm)

  implicit def readerEnum[T <: odm.Enumerable]: Writer[T] = macro ConvertersImpl.readerEnumImpl[T]

  implicit def readerWriterEnum[T <: odm.Enumerable]: ReadWriter[T] = macro ConvertersImpl.readerWriterEnumImpl[T]


}
