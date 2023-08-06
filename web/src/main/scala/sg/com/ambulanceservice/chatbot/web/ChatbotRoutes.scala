package sg.com.ambulanceservice.chatbot.web

import com.mongodb.client.model.{Filters, InsertOneOptions, UpdateOptions, Updates}
import org.bson.Document
import sg.com.ambulanceservice.chatbot.Mongo
import sg.com.ambulanceservice.chatbot.api.Converters
import sg.com.ambulanceservice.chatbot.collections.cases
import sg.com.ambulanceservice.chatbot.collections.cases.CaseModel

case class VisitorCount(name: String, count: Int = 0)

object ChatbotRoutes extends cask.MainRoutes {
  def toVisitorCount(d: Document): VisitorCount = {
    VisitorCount(
      d.getString("name"),
      d.getInteger("count", 0)
    )
  }

  // This can probably be macro-ified...
  implicit val ucs: upickle.default.ReadWriter[cases.CaseSchema] = upickle.default.macroRW[cases.CaseSchema]
  implicit val uca: upickle.default.ReadWriter[cases.Address] = upickle.default.macroRW[cases.Address]
  implicit val ucp: upickle.default.ReadWriter[cases.PatientDetails] = upickle.default.macroRW[cases.PatientDetails]
  implicit val ucb: upickle.default.ReadWriter[cases.BillingDetails] = upickle.default.macroRW[cases.BillingDetails]
  implicit val ucl: upickle.default.ReadWriter[cases.LatLng] = upickle.default.macroRW[cases.LatLng]
  implicit val ucpm: upickle.default.ReadWriter[cases.PaymentMethod] = Converters.readerWriterEnum[cases.PaymentMethod]

  // Create a new case
  @cask.get("/")
  def root() = {
    ""
  }

  // Create a new case
  @cask.postJson("/cases")
  def createCase(body: cases.CaseSchema) = {
    new CaseModel(Mongo.getDatabase)
      .insert(body, new InsertOneOptions())
    ""
  }

  // Fetch a list of cases
  @cask.get("/cases")
  def listCases() = {
    cask.model.Response("", 500)
  }

  // Fetch a single cases
  @cask.get("/cases/:caseId")
  def getCase(caseId: String) = {
    cask.model.Response("", 500)
  }

  // Some other functions ...
  // - Send out a case?
  // - Cancel a case?
  //

  initialize()
}