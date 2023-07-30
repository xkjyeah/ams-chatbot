package sg.com.ambulanceservice.chatbot.collections

import com.mongodb.client.MongoDatabase
import org.telegram.telegrambots.meta.api.objects.Message
import sg.com.ambulanceservice.chatbot.odm.annotations.serializedTypeName
import sg.com.ambulanceservice.chatbot.odm.{AbstractModelDefinition, ConvertToBson, Enumerable, FieldIndexDefinition, IndexDefinition}

object cases {

  import sg.com.ambulanceservice.chatbot.odm.Converters._

  sealed trait PaymentMethod extends Enumerable[String]

  case class Cash() extends PaymentMethod {
    val serializedForm = "cash"
  }

  case class PayNow() extends PaymentMethod {
    val serializedForm = "paynow"
  }

  case class Billing() extends PaymentMethod {
    val serializedForm = "billing"
  }

  case class Address(
                      from: String,
                      fromPostalCode: String,
                    )

  case class PatientDetails(
                             name: String,
                             partialId: String,
                             weight: String,
                             complaint: String,
                           )

  case class CallerDetails(
                            name: String,
                            telephone: String,
                          )

  case class BillingDetails(
                             price: BigDecimal,
                             paymentMethod: PaymentMethod,
                             billTo: Option[String],
                           )

  case class CaseSchema(
                         from: Address,
                         to: Address,
                         time: Long,

                         patientDetails: PatientDetails,
                         billingDetails: BillingDetails,
                         remarks: String,

                         // For multiple trips, link by ID to the first trip
                         linkedCase: Option[String],
                         // The persons assigned to this case
                         assignees: Option[List[String]],
                       )

  class CaseModel(database: MongoDatabase) extends AbstractModelDefinition[CaseSchema](database) {
    override def collectionName: String = "cases"

    override def indexes: Seq[IndexDefinition] = List(
      IndexDefinition(FieldIndexDefinition("time", true))
    )
  }
}
