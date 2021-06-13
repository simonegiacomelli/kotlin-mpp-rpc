import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import rpc.ApiConf
import rpc.ApiRequestSum
import rpc.ApiResponseSum
import rpc.ContextHandlers
import java.time.Duration
import java.util.*

val contextHandler = ContextHandlers<PipelineContext<Unit, ApplicationCall>>()
fun main() {
    contextHandler.register { req: ApiRequestSum, context ->
        ApiResponseSum(req.a + req.b)
    }
    embeddedServer(CIO, port = 8080, module = Application::module).apply { start(wait = true) }
}

fun Application.module() {
    install(WebSockets)
    install(CORS) {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        anyHost()
        host("my-host")
        // host("my-host:80")
        // host("my-host", subDomains = listOf("www"))
        // host("my-host", schemes = listOf("http", "https"))
        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAge = Duration.ofDays(1)
    }

    var counter = 0
    routing {
        registerApiServerRoute()
        get("/plain") {
            call.respondText("Hello World2! ${++counter}")
        }
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
//        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
//            while (true) {
//                delay(1000)
//                println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
//                connections.forEach {
//                    it.session.send("ciao ciccio! ${++counter}")
//                }
//            }
//        }

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

fun Routing.registerApiServerRoute() {

    val path = ApiConf.baseUrl("{api_name}")
    post("$path") {
        try {
            val apiName = call.parameters["api_name"]!!
            println("dispatching $apiName")
            val serializedResponse =
                contextHandler.dispatch(apiName, call.receiveText(), this)
            call.respondText("success=1\n\n$serializedResponse", ContentType.Text.Plain)
        } catch (ex: Exception) {
            val text = "success=0\n\n${ex.stackTraceToString()}"
            println("handling exception [[$text]] ")
            call.respondText(
                text = text,
                status = HttpStatusCode.InternalServerError,
                contentType = ContentType.Text.Plain
            )
        }
    }
}