package sg.com.ambulanceservice.chatbot.api

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import sg.com.ambulanceservice.chatbot.odm

class ConvertersImpl(val c: Context) {

  import c.universe._

  def p[T](t: T): T = {
    println(t)
    t
  }

  def readerEnumImpl[U <: odm.Enumerable : WeakTypeTag]: Tree = {
    import upickle.default._

    val tType = weakTypeOf[U]
    val sealedAncestor = tType.baseClasses.find(c => c.asClass.isSealed && c.asClass.baseClasses.contains(typeOf[odm.Enumerable].typeSymbol)).get
    val knownSubclassesAndSerializedValues = sealedAncestor.asClass.knownDirectSubclasses
      .map { subclass =>
        if (!subclass.isModuleClass) {
          throw new RuntimeException(s"#{subclass} should be a case object!")
        }
        q"""
        (${subclass.asClass.module}.serializedForm, ${subclass.asClass.module})
        """
      }

    val converterType = weakTypeOf[Reader[String]]
    val converter = c.inferImplicitValue(converterType)
    if (converter.isEmpty) {
      throw new RuntimeException(
        s"""
      Could not find implicit ${converterType} type in the calling context.
      """)
    }
    p(
      q"""
        ${converter}.map(Map(..${knownSubclassesAndSerializedValues}))
      """)
  }

  def readerWriterEnumImpl[U <: odm.Enumerable : WeakTypeTag]: Tree = {
    val tType = weakTypeOf[U]
    q"""
    import sg.com.ambulanceservice.chatbot.api.Converters._

    upickle.default.ReadWriter.join(
      implicitly[upickle.default.Reader[${tType}]],
      implicitly[upickle.default.Writer[${tType}]]
    )
    """
  }

}
