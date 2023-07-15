package sg.com.ambulanceservice.chatbot.odm


import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.{Filters, FindOneAndUpdateOptions, Updates}
import org.bson.conversions.Bson

case class FieldIndexDefinition(field: String, ascending: Boolean)

case class IndexDefinition(fields: FieldIndexDefinition, unique: Boolean = false)

object AbstractModelDefinition {
  type FilterType = Map[String, Any]

  def toFilter(m: Map[String, Any]): Bson = {
    val filters: List[org.bson.conversions.Bson] = m.map { case (key, value) => Filters.eq(key, Converters.convertBasicTypes.serialize(value)) }.toList
    Filters.and(filters: _*)
  }
}

abstract class AbstractModelDefinition[T](database: MongoDatabase)(implicit val converter: ConvertToBson[T]) {

  def collectionName: String

  def indexes: Seq[IndexDefinition]

  def loadOne(filter: AbstractModelDefinition.FilterType): Option[T] = {
    Option(database.getCollection("collectionName")
      .find(AbstractModelDefinition.toFilter(filter))
      .first)
      .map(s => converter.deserialize(s.toBsonDocument))
  }

  def findOneAndUpdate(filter: AbstractModelDefinition.FilterType, t: T, options: FindOneAndUpdateOptions) = {
    import scala.jdk.CollectionConverters._

    val ser = converter.serialize(t).asDocument()
    val updates = Updates.combine(ser.keySet().asScala.toList.map { key =>
      Updates.set(key, ser.get(key))
    }: _*)

    database.getCollection("collectionName")
      .findOneAndUpdate(
        AbstractModelDefinition.toFilter(filter),
        updates,
        options
      )
  }
}
