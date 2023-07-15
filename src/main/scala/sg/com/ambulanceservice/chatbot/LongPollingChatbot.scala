package sg.com.ambulanceservice.chatbot

import org.mongodb.scala.model.FindOneAndUpdateOptions
import org.telegram.telegrambots.bots.{DefaultBotOptions, TelegramLongPollingBot}
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import sg.com.ambulanceservice.chatbot.collections.conversations
import sg.com.ambulanceservice.chatbot.collections.conversations.ConversationState

class LongPollingChatbot(options: DefaultBotOptions, botToken: String) extends TelegramLongPollingBot(options, botToken) {
  private val conversationsCollection = new conversations.ConversationModel(sg.com.ambulanceservice.chatbot.Mongo.getDatabase)

  def getConversationState(chatId: Long): conversations.ConversationState = {
    conversationsCollection.loadOne(Map("chatId" -> chatId))
      .map(_.conversationState)
      .getOrElse(conversations.ConversationState.Initial())
  }

  override def onUpdateReceived(update: Update): Unit = {
    val chatId = update.getMessage.getChatId

    // First determine what state the conversation is in
    val conversationState = getConversationState(chatId)

    val (newState, suggestedMessages) = handleAction(conversationState, chatId, update)

    suggestedMessages.foreach {
      case SuggestedMessage(message, chatId) =>
        val sendMessage = new SendMessage()
        sendMessage.setChatId(chatId)
        sendMessage.setText(message)
        try {
          execute[Message, SendMessage](sendMessage)
        } catch {
          case s: RuntimeException => println(s.getMessage())
        }
    }

    conversationsCollection.findOneAndUpdate(
      Map("chatId" -> chatId),
      conversations.ConversationSchema(
        chatId,
        newState,
      ),
      FindOneAndUpdateOptions().upsert(true)
    )
  }

  override def getBotUsername: String = "ams_pte_ltd_bot"

  def handleAction(currentState: ConversationState, chatId: Long, update: Update): (ConversationState, List[SuggestedMessage]) = {
    currentState match {
      case conversations.ConversationState.Initial() =>
        (
          conversations.ConversationState.AwaitingHello(),
          List(
            SuggestedMessage("Welcome to Greet Bot. What's your name?", update.getMessage.getChatId)
          )
        )

      case conversations.ConversationState.AwaitingHello() =>
        (
          conversations.ConversationState.Initial(),
          List(
            SuggestedMessage(s"Hello, ${update.getMessage.getText}", update.getMessage.getChatId)
          )
        )
    }
  }
}