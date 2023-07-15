package sg.com.ambulanceservice.chatbot

import com.mongodb.client.{MongoClient, MongoClients, MongoDatabase}

object Mongo {

  val DEFAULT_MONGODB_URL = "mongodb://localhost:27017"

  lazy val getClient: MongoClient = {
    MongoClients.create(sys.env.get("MONGODB_URL").getOrElse(DEFAULT_MONGODB_URL))
  }

  def getDatabase: MongoDatabase = {
    getClient.getDatabase("test-app")
  }
}
