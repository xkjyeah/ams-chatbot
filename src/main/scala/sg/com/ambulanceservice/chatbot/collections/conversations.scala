package sg.com.ambulanceservice.chatbot.collections

import org.mongodb.scala.MongoDatabase
import org.telegram.telegrambots.meta.api.objects.Message
import sg.com.ambulanceservice.chatbot.odm.annotations.serializedTypeName
import sg.com.ambulanceservice.chatbot.odm.{AbstractModelDefinition, ConvertToBson, FieldIndexDefinition, IndexDefinition}

object conversations {

  import sg.com.ambulanceservice.chatbot.odm.Converters._

  sealed trait ConversationState {
    def suggestedMessages(prevState: ConversationState): List[Message] = List()
  }

  object ConversationState {
    @serializedTypeName("initial")
    case class Initial() extends ConversationState

    @serializedTypeName("awaiting_hello")
    case class AwaitingHello() extends ConversationState
  }

  case class ConversationSchema(
                                 chatId: Long,
                                 conversationState: ConversationState,
                               )

  implicit val convertConversationState = convertSealedTrait[ConversationState]
  implicit val cs: ConvertToBson[ConversationSchema] = convertCaseClass[ConversationSchema]

  class ConversationModel(database: MongoDatabase) extends AbstractModelDefinition[ConversationSchema](database) {
    override def collectionName: String = "conversations"

    override def indexes: Seq[IndexDefinition] = List(
      IndexDefinition(FieldIndexDefinition("chatId", true))
    )
  }
}
