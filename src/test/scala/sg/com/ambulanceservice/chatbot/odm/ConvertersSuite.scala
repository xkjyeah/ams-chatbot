package sg.com.ambulanceservice.chatbot.odm

import org.bson.BsonDocument

class ConvertersSuite extends munit.FunSuite {
  test("Converts a case class with all the known basic types and back") {
    import Converters._

    case class Nested(s: String)
    case class Simple(s: String, i: Int, l: Long, f: Float, d: Double, b: Boolean, n: Nested, ll: List[Int])

    val s = Simple("123", 456, -5L, 0.5f, 0.555, false, Nested("abc"), List(-1, 0, 2))
    val converter = implicitly[ConvertToBson[Simple]]

    val serialized = converter.serialize(s)
    val deserialized = converter.deserialize(serialized)

    assertEquals(s, deserialized)
  }

  test("Converts sealed traits with convertSealedTrait") {
    import Converters._

    sealed trait Bla

    @annotations.serializedTypeName("nested")
    case class Nested(s: String) extends Bla

    @annotations.serializedTypeName("simple")
    case class Simple(s: String, i: Int, l: Long, f: Float, d: Double, b: Boolean, n: Nested, ll: List[Int]) extends Bla

    //    implicit val convertBla: ConvertToBson[Bla] = convertSealedTrait[Bla]

    val s = Simple("123", 456, -5L, 0.5f, 0.555, false, Nested("abc"), List(-1, 0, 2))
    val n = Nested("bla")

    val converter: ConvertToBson[Bla] = convertSealedTrait[Bla]
    assert(n == converter.deserialize(converter.serialize(n)))
    assert(s == converter.deserialize(converter.serialize(s)))
  }

  test("convertSealedTrait behaves identically as converter of case classes that are descendents of AutoConvert") {
    import Converters._

    sealed trait Bla extends AutoConvert

    @annotations.serializedTypeName("nested")
    case class Nested(s: String) extends Bla

    val n = Nested("bla")

    val traitConverter: ConvertToBson[Bla] = convertSealedTrait[Bla]
    val classConverter = implicitly[ConvertToBson[Nested]]
    assert(traitConverter.serialize(n) == classConverter.serialize(n))
  }

  // This is not ideal -- Scala already has mechanisms to
  // set default values. However, it's very difficult to utilize those
  // default values because we need to know at compile-time which keys exist in
  // the document (which we can't...)
  test("sets default values in case class if variable is not defined") {
    import Converters._

    case class Nested(@annotations.defaultValue("Hola") s: String)

    val classConverter = implicitly[ConvertToBson[Nested]]

    val s: BsonDocument = new BsonDocument()

    s.keySet.contains()

    assert(Nested("Hola") == classConverter.deserialize(s))
  }
}
