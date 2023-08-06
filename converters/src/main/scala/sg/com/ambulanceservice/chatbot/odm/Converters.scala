package sg.com.ambulanceservice.chatbot.odm

import scala.language.experimental.macros

object Converters {
  implicit val convertInt: ConvertToBson[Int] = new ConvertToBson[Int](
    new org.bson.BsonInt32(_),
    _.asInt32.getValue()
  )
  implicit val convertLong: ConvertToBson[Long] = new ConvertToBson[Long](
    new org.bson.BsonInt64(_),
    _.asInt64.getValue
  )
  implicit val convertBigDecimal: ConvertToBson[BigDecimal] = new ConvertToBson[BigDecimal](
    s => new org.bson.BsonString(s.toString()),
    s => math.BigDecimal(s.asString.getValue)
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

  val convertBasicTypes = new ConvertToBson[Any](
    {
      case n: Long => convertLong.serialize(n)
      case n: Int => convertInt.serialize(n)
      case n: Double => convertDouble.serialize(n)
      case n: Float => convertFloat.serialize(n)
      case n: String => convertString.serialize(n)
      case n: Boolean => convertBool.serialize(n)
    },
    _ => throw new UnsupportedOperationException("convertBasicTypes is only intended as a one-way conversion for filters")
  )

  implicit def convertOption[T](implicit converter: ConvertToBson[T]): ConvertToBson[Option[T]] = {
    new ConvertToBson[Option[T]](
      {
        case Some(x) => converter.serialize(x)
        case None => new org.bson.BsonNull
      },
      {
        case b if b.isNull => None
        case b => Some(converter.deserialize(b))
      }
    )
  }

  implicit def convertArray[T](implicit converter: ConvertToBson[T]): ConvertToBson[List[T]] = {
    import scala.jdk.CollectionConverters._

    new ConvertToBson[List[T]](
      (s: List[T]) => new org.bson.BsonArray(s.map(converter.serialize).asJava),
      (b: org.bson.BsonValue) => b.asArray().getValues.asScala.toList.map(converter.deserialize)
    )
  }

  implicit def convertEnumerable[U <: Enumerable]: ConvertToBson[U] =
  macro ConvertersImpl.convertEnumerableI[U]

  implicit def convertCaseClass[T <: scala.Product]: ConvertToBson[T] = macro ConvertersImpl.converterFromCaseClass[T]

  // If you are lazy, and you don't mind it polluting your
  // class hierarchy, extend AutoConvert
  // Otherwise, manually create your implicits
  // implicit val bla = Converters.convertSealedTrait(...)
  def convertSealedTrait[T]: ConvertToBson[T] = macro ConvertersImpl.converterFromSealedTrait[T]
}
