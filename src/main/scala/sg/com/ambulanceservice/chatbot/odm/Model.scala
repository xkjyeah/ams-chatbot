package sg.com.ambulanceservice.chatbot.odm

import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, UpdateOptions, Updates}

import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

case class FieldIndexDefinition(field: String, ascending: Boolean)

case class IndexDefinition(fields: FieldIndexDefinition, unique: Boolean = false)

object AbstractModelDefinition {
  type FilterType = Map[String, Any]

  def toFilter(m: Map[String, Any]): Bson = {
    val filters: List[org.bson.conversions.Bson] = m.map { case (key, value) => Filters.equal(key, Converters.convertBasicTypes.serialize(value)) }.toList
    Filters.and(filters: _*)
  }
}

abstract class AbstractModelDefinition[T](database: MongoDatabase)(implicit val converter: ConvertToBson[T]) {

  def collectionName: String

  def indexes: Seq[IndexDefinition]

  def loadOne(filter: AbstractModelDefinition.FilterType): Option[T] = {
    val fut: Future[Option[T]] = database.getCollection("collectionName")
      .find(AbstractModelDefinition.toFilter(filter))
      .headOption
      .map(_.map(s => converter.deserialize(s.toBsonDocument)))
    Await.result(fut, Duration(30, TimeUnit.SECONDS))
  }

  def findOneAndUpdate(filter: AbstractModelDefinition.FilterType, t: T, options: FindOneAndUpdateOptions) = {
    import scala.jdk.CollectionConverters._

    val ser = converter.serialize(t).asDocument()
    val updates = ser.keySet().asScala.toList.map { key =>
      Updates.set(key, ser.get(key))
    }

    val fut = database.getCollection("collectionName")
      .findOneAndUpdate(
        AbstractModelDefinition.toFilter(filter),
        updates,
        options
      )
      .toFuture()
    Await.result(fut, Duration(30, TimeUnit.SECONDS))
  }
}
