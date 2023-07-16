package sg.com.ambulanceservice.chatbot.odm

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

class ConvertersImpl(val c: Context) {

  import c.universe._

  def p[T](t: T): T = {
    println(t)
    t
  }

  val TYPE_KEY = "__type"

  def converterFromSealedTrait[T](implicit tag: c.WeakTypeTag[T]): c.Expr[ConvertToBson[T]] = {
    val klass = tag.tpe.typeSymbol.asClass
    if (!klass.isSealed) {
      throw new RuntimeException(s"Can't serialize ${tag.tpe.toString}")
    } else {
      makeConverterFromSealedTrait[T](tag.tpe)
    }
  }

  def makeConverterFromSealedTrait[T](tType: Type): c.Expr[ConvertToBson[T]] = {
    val klass = tType.typeSymbol.asClass
    val knownSubclasses = klass.knownDirectSubclasses

    // Can only serialize subclasses with the serialized type name annotation
    val classToTypeName = knownSubclasses.flatMap { subclass =>
      val result = subclass.asClass.annotations.collectFirst {
        case a if a.tree.tpe.typeSymbol == c.typeOf[annotations.serializedTypeName].typeSymbol =>
          val Literal(Constant(z)) :: _ = a.tree.children.tail
          subclass -> z.asInstanceOf[String]
      }

      // Helpful compiler hints
      result match {
        case Some(_) => ()
        case None => System.err.println(s"Subclass ${subclass} is missing the annotation annotations.serializedTypeName and therefore won't be serialized")
      }

      result
    }

    val combinedSerializerCases = classToTypeName.map { case (klass, typeName) =>
      cq"""
          z: ${klass.asType} =>
            val doc = ${serializeFromCaseClass(q"z", klass.asType.toType)}
            doc.append(${TYPE_KEY}, new org.bson.BsonString(${typeName}))
            doc
        """
    }

    val combinedDeserializerCases = classToTypeName.map { case (klass, typeName) =>
      cq"""
          z if z == ${typeName} => ${deserializeToCaseClass(q"doc", klass.asType.toType)}
        """
    }

    p(c.Expr[ConvertToBson[T]](
      q"""
        new sg.com.ambulanceservice.chatbot.odm.ConvertToBson[${tType}](
          { case ..${combinedSerializerCases} },
          (z: org.bson.BsonValue) => {
            val doc = z.asDocument
            doc.getString(${TYPE_KEY}).getValue match {
              case ..${combinedDeserializerCases}
            }
          }
        )
      """))
  }

  def converterFromCaseClass[T](implicit tag: c.WeakTypeTag[T]): c.Expr[ConvertToBson[T]] = {
    val tType = tag.tpe
    val sealedAutoConvertAncestor = tType.baseClasses.find {
      baseClass =>
        baseClass.asClass.isSealed &&
          baseClass.asClass.baseClasses.exists { ancestorOfBaseClass =>
            ancestorOfBaseClass.asClass == c.typeOf[AutoConvert].typeSymbol.asClass
          }
    }
    // For classes that are descendents of an AutoConvert,
    // use the sealed trait converter

    sealedAutoConvertAncestor match {
      case Some(ancestorType) =>
        c.Expr[ConvertToBson[T]](
          q"""
        val sealedTraitConverter = ${makeConverterFromSealedTrait(ancestorType.asType.toType).tree}

        new sg.com.ambulanceservice.chatbot.odm.ConvertToBson[${tType}](
          sealedTraitConverter.serialize,
          (z: org.bson.BsonValue) => sealedTraitConverter.deserialize(z).asInstanceOf[${tType}]
        )
        """)

      case None =>
        p(c.Expr[ConvertToBson[T]](
          q"""
            new sg.com.ambulanceservice.chatbot.odm.ConvertToBson[${tType}](
              (z: ${tType}) => { ${serializeFromCaseClass(q"z", tType)} },
              (z: org.bson.BsonValue) => { ${deserializeToCaseClass(q"z", tType)} }
            )
          """))
    }
  }

  private def serializeFromCaseClass(t: c.Tree, tpe: Type): c.Expr[org.bson.BsonValue] = {
    val params = tpe.baseClasses.head.asClass.primaryConstructor.asMethod.paramLists.head

    c.Expr[org.bson.BsonDocument](
      q"""
      val doc = new ${c.symbolOf[org.bson.BsonDocument]}()

      ..${
        params.map { paramSym =>
          val tpe = c.weakTypeOf[ConvertToBson[_]]
          val implicitType = appliedType(tpe, List(paramSym.typeSignature))

          if (c.inferImplicitValue(implicitType).isEmpty) {
            throw new RuntimeException(
              s"""
            Could not find implicit ${implicitType} type for param ${paramSym.name} with type ${paramSym.typeSignature} in the calling context.
            """)
          }

          q"""
            val converter = ${c.inferImplicitValue(implicitType)}
            doc.append(
              ${paramSym.asTerm.name.toString},
              converter.serialize(${t}.${paramSym.asTerm.name})
            )
          """
        }
      }
      doc
      """)
  }

  private def deserializeToCaseClass[T](t: c.Tree, tpe: Type): c.Expr[T] = {
    val constructor = tpe.baseClasses.head.asClass.primaryConstructor
    val constructorParams = constructor.asMethod.paramLists.head

    c.Expr[T](
      q"""
    new ${tpe.baseClasses.head}(
      ..${
        constructorParams.map { paramSym =>
          val tpe = c.weakTypeOf[ConvertToBson[_]]
          val implicitType = appliedType(tpe, List(paramSym.typeSignature))
          val defaultValueExpression: Option[Tree] = paramSym.asTerm.annotations.collectFirst {
            case a if a.tree.tpe.typeSymbol == typeOf[annotations.defaultValue].typeSymbol =>
              a.tree.children.tail.head
          }

          val deserializeExpression = defaultValueExpression match {
            case Some(x) => q"""
              if (d.keySet.contains(${paramSym.name.toString})) {
                converter.deserialize(d.get(${paramSym.name.toString}))
              } else {
                ${x}
              }
            """
            case None => q"""
              converter.deserialize(d.get(${paramSym.name.toString}))
            """
          }

          q"""{
            val converter = ${c.inferImplicitValue(implicitType)}
            val d = ${t}.asInstanceOf[org.bson.BsonDocument]
            ${deserializeExpression}
          }"""
        }
      }
    )
    """)
  }
}
