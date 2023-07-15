package sg.com.ambulanceservice.chatbot

import org.mongodb.scala.bson.BsonDocument
import sg.com.ambulanceservice.chatbot.odm.{AutoConvert, ConvertToBson, ConvertersImpl}

import scala.language.experimental.macros
import scala.reflect.ClassTag

object Converters {
  implicit val convertInt: ConvertToBson[Int] = new ConvertToBson[Int](
    new org.bson.BsonInt32(_),
    _.asInt32.getValue()
  )
  implicit val convertLong: ConvertToBson[Long] = new ConvertToBson[Long](
    new org.bson.BsonInt64(_),
    _.asInt64.getValue
  )
  implicit val convertDouble: ConvertToBson[Double] = new ConvertToBson[Double](
    new org.bson.BsonDouble(_),
    _.asDouble.getValue
  )
  implicit val convertFloat: ConvertToBson[Float] = new ConvertToBson[Float](
    new org.bson.BsonDouble(_),
    _.asDouble.getValue.toFloat
  )
  implicit val convertString: ConvertToBson[String] = new ConvertToBson[String](
    new org.bson.BsonString(_),
    _.asString.getValue
  )
  implicit val convertBool: ConvertToBson[Boolean] = new ConvertToBson[Boolean](
    new org.bson.BsonBoolean(_),
    _.asBoolean.getValue
  )

  implicit def convertArray[T](implicit converter: ConvertToBson[T]): ConvertToBson[List[T]] = {
    import scala.jdk.CollectionConverters._

    new ConvertToBson[List[T]](
      (s: Seq[T]) => new org.bson.BsonArray(s.map(converter.serialize).asJava),
      (b: org.bson.BsonValue) => b.asArray().getValues.asScala.toList.map(converter.deserialize)
    )
  }

  implicit def convertCaseClass[T <: scala.Product]: ConvertToBson[T] =
  macro ConvertersImpl.converterFromCaseClass[T]

  def convertSealedTrait[T]: ConvertToBson[T] =
  macro ConvertersImpl.converterFromSealedTrait[T]
}


object TestMacro extends App {
  sealed trait SomeSealed extends AutoConvert

  @odm.annotations.serializedTypeName("test")
  case class Test(s: String, i: Int) extends SomeSealed

  import Converters._

  val cs = implicitly[ConvertToBson[List[Int]]]
  //  val cs2 = implicitly[ConvertToBson[SomeSealed]]
  val cs3 = implicitly[ConvertToBson[Test]]

  convertSealedTrait[SomeSealed]

  val c = cs.serialize(List(1, 2, 3))
  println(c)
}
