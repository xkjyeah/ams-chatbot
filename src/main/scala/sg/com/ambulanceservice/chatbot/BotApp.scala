package sg.com.ambulanceservice.chatbot

import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object BotApp extends App {
  val botsApi = new TelegramBotsApi(classOf[DefaultBotSession])
  botsApi.registerBot(new LongPollingChatbot(
    new DefaultBotOptions(),
    sys.env("TELEGRAM_TOKEN")
  ))
}
