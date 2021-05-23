import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(
        CIO,
//        watchPaths = listOf("."),
        port = 8080,
        module = Application::mymodule,
    ).apply {

        start(wait = true)
    }
}

fun Application.mymodule() {
    install(WebSockets)
    var counter = 0
    routing {
        get("/plain") {
            call.respondText("Hello World2! ${++counter}")
        }
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            while (true) {
                delay(1000)
                println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
                connections.forEach {
                    it.session.send("ciao ciccio! ${++counter}")
                }
            }
        }
        webSocket("/chat") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("You are connected! There are ${connections.count()} users here.")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    connections.forEach {
                        it.session.send(textWithUsername)
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }
}