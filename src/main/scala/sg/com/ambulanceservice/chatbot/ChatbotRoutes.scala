package sg.com.ambulanceservice.chatbot

import com.mongodb.client.model.{Filters, UpdateOptions, Updates}
import org.bson.Document

case class VisitorCount(name: String, count: Int = 0)

object ChatbotRoutes extends cask.MainRoutes {
  def toVisitorCount(d: Document): VisitorCount = {
    VisitorCount(
      d.getString("name"),
      d.getInteger("count", 0)
    )
  }

  @cask.get("/hello/:name")
  def hello(name: String) = {
    val result = Option(Mongo.getDatabase.getCollection("visits").find(
      Filters.eq("name", name)
    )
      .map(toVisitorCount)
      .first())
      .getOrElse(VisitorCount(name, -1))

    Mongo.getDatabase.getCollection("visits")
      .updateOne(
        Filters.eq("name", name),
        Updates.combine(Updates.set("count", result.count + 1)),
        new UpdateOptions().upsert(true)
      )

    ujson.Obj(
      "message" -> s"Hello, ${name} x${result.count}"
    )
  }

  initialize()
}