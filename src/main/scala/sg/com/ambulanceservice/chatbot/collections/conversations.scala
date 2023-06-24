package sg.com.ambulanceservice.chatbot.collections

import org.telegram.telegrambots.meta.api.objects.Message

object conversations {
  sealed trait ConversationState {
    def suggestedMessages(prevState: ConversationState): List[Message] = List()
  }

  case class Initial() extends ConversationState

  case class AwaitingHello() extends ConversationState
}
