package sg.com.ambulanceservice.chatbot

import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala._

//import scala.collection.JavaConverters._


object Mongo {

  val DEFAULT_MONGODB_URL = "mongodb://localhost:27017"

  lazy val getClient: MongoClient = {
    MongoClient(sys.env.get("MONGODB_URL").getOrElse(DEFAULT_MONGODB_URL))
  }

  def getDatabase = {

    val codecRegistry = CodecRegistries.fromRegistries(
      CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
      MongoClient.DEFAULT_CODEC_REGISTRY)

    getClient.getDatabase("test-app")
      .withCodecRegistry(codecRegistry)
  }
}
