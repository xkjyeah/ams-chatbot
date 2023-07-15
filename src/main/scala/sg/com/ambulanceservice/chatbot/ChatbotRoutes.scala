package sg.com.ambulanceservice.chatbot

import cask.internal.Conversion
import cask.model.Response
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{UpdateOptions, Updates}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class VisitorCount(name: String, count: Int = 0)

object ChatbotRoutes extends cask.MainRoutes {
  implicit def unwrapFuture[T](implicit converter: cask.internal.Conversion[T, Response.Raw]): cask.internal.Conversion[Future[T], Response.Raw] = {
    Conversion.create(
      f => converter.f(Await.result(f, Duration(30, TimeUnit.SECONDS)))
    )
  }

  def toVisitorCount(d: Document): VisitorCount = {
    VisitorCount(
      d.getString("name"),
      d.getInteger("count", 0)
    )
  }

  @cask.get("/hello/:name")
  def hello(name: String) = {
    for {
      result <-
        Mongo.getDatabase.getCollection("visits").find(
          equal("name", name)
        ).headOption()
          .map(_.map(toVisitorCount)
            .getOrElse(VisitorCount(name, -1)))
      _ <- Mongo.getDatabase.getCollection("visits")
        .updateOne(
          equal("name", name),
          Updates.combine(set("count", result.count + 1)),
          UpdateOptions().upsert(true))
        .toFuture()
    } yield ujson.Obj(
      "message" -> s"Hello, ${name} x${result.count}"
    )
  }

  initialize()
}