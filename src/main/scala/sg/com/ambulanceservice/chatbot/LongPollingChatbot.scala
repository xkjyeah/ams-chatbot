package sg.com.ambulanceservice.chatbot

import org.telegram.telegrambots.bots.{DefaultBotOptions, TelegramLongPollingBot}
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import sg.com.ambulanceservice.chatbot.collections.conversations
import sg.com.ambulanceservice.chatbot.collections.conversations.ConversationState

import scala.collection.mutable

class LongPollingChatbot(options: DefaultBotOptions, botToken: String) extends TelegramLongPollingBot(options, botToken) {
  private val conversationStates = mutable.HashMap[Long, conversations.ConversationState]()

  def getConversationState(chatId: Long): conversations.ConversationState = {
    // Right now, just use a map
    // Next time save to database
    conversationStates.getOrElse(chatId, conversations.Initial())
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

    conversationStates.put(chatId, newState)
  }

  override def getBotUsername: String = "ams_pte_ltd_bot"

  def handleAction(currentState: ConversationState, chatId: Long, update: Update): (ConversationState, List[SuggestedMessage]) = {
    currentState match {
      case conversations.Initial() =>
        (
          conversations.AwaitingHello(),
          List(
            SuggestedMessage("Welcome to Greet Bot. What's your name?", update.getMessage.getChatId)
          )
        )

      case conversations.AwaitingHello() =>
        (
          conversations.Initial(),
          List(
            SuggestedMessage(s"Hello, ${update.getMessage.getText}", update.getMessage.getChatId)
          )
        )
    }
  }
}