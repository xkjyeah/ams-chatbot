package sg.com.ambulanceservice.chatbot

import com.mongodb.client.model.FindOneAndUpdateOptions
import org.telegram.telegrambots.bots.{DefaultBotOptions, TelegramLongPollingBot}
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import sg.com.ambulanceservice.chatbot.collections.conversations
import sg.com.ambulanceservice.chatbot.collections.conversations.ConversationState

object Matchers {
  object WantRestart {
    def unapply(text: String): Boolean = {
      text.equalsIgnoreCase("restart")
    }
  }
}

class LongPollingChatbot(options: DefaultBotOptions, botToken: String) extends TelegramLongPollingBot(options, botToken) {
  private val conversationsCollection = new conversations.ConversationModel(Mongo.getDatabase)

  def getConversationState(chatId: Long): conversations.ConversationState = {
    conversationsCollection.loadOne(Map("chatId" -> chatId))
      .map(_.conversationState)
      .getOrElse(conversations.ConversationState.Initial())
  }

  override def onUpdateReceived(update: Update): Unit = {
    val chatId = update.getMessage.getChatId

    // First determine what state the conversation is in
    val session = Mongo.getClient.startSession()
    val suggestedMessages = session.withTransaction { () =>
      val conversationState = getConversationState(chatId)
      val (nextState, suggestedMessages) = handleAction(conversationState, chatId, update)

      conversationsCollection.findOneAndUpdate(
        Map("chatId" -> chatId),
        conversations.ConversationSchema(
          chatId,
          nextState,
        ),
        new FindOneAndUpdateOptions().upsert(true)
      )

      suggestedMessages
    }

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
  }

  override def getBotUsername: String = "ams_pte_ltd_bot"

  def handleAction(currentState: ConversationState, chatId: Long, update: Update): (ConversationState, List[SuggestedMessage]) = {
    (currentState, update.getMessage.getText) match {
      case (_, Matchers.WantRestart()) =>
        (
          conversations.ConversationState.Initial(),
          List(
            SuggestedMessage(s"Restarting from the beginning...", update.getMessage.getChatId)
          )
        )

      case (conversations.ConversationState.Initial(), _) =>
        (
          conversations.ConversationState.AwaitingHello(),
          List(
            SuggestedMessage("Welcome to Greet Bot. What's your name?", update.getMessage.getChatId)
          )
        )

      case (conversations.ConversationState.AwaitingHello(), text) =>
        (
          conversations.ConversationState.WithName(text),
          List(
            SuggestedMessage(s"Hello, ${text}. What can I do for you?", update.getMessage.getChatId)
          )
        )

      case (conversations.ConversationState.WithName(name), text) =>
        (
          conversations.ConversationState.WithName(name),
          List(
            SuggestedMessage(s"Yes ${name}, I can certainly ${text}", update.getMessage.getChatId)
          )
        )
    }
  }
}