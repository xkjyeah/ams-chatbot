package sg.com.ambulanceservice.chatbot.web;

import io.undertow.Undertow

class ChatbotRoutesSuite extends munit.FunSuite {
  def withServer[T](example: cask.main.Main)(f: String => T): T = {
    val server = Undertow.builder
      .addHttpListener(8081, "localhost")
      .setHandler(example.defaultHandler)
      .build
    server.start()
    val res = try {
      f("http://localhost:8081")
    }
    finally {
      server.stop()
    }
    res
  }

  def runIt[T](f: String => T) = withServer[T](ChatbotRoutes)(f)

  test("Basic server startup") {
    runIt { host =>
      println(host)

      val success = requests.get(host)

      assertEquals(success.text, "")
      assertEquals(success.statusCode, 200)
    }
  }

  test("Mongo read/write") {
    runIt { host =>
      val success = requests.get(host + "/cases")
      assertEquals((200, "[]"), (success.statusCode, success.text))
    }
  }
}