import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.MessageEvent
import org.w3c.dom.Node
import org.w3c.dom.WebSocket
import rpc.ApiRequestMul
import rpc.ApiRequestSum
import rpc.ApiResponseMul
import rpc.ContextHandlers

val contextHandler = ContextHandlers<MessageEvent>()

fun main() {

    contextHandler.register { req: ApiRequestMul ->
        ApiResponseMul(req.a * req.b)
    }

    window.onload = { document.body?.sayHello() }

    openWebSocket()

    GlobalScope.launch {
        testJvmApi()
    }


}

suspend fun testJvmApi() {
    console.log("Using api rpc")
    val response = api.send(ApiRequestSum(5, 7))
    console.log("sum is: " + response.sum)

}


fun openWebSocket() {
    val socket = WebSocket("ws://localhost:8080/ws1")
    fun onMessage(event: MessageEvent) {
        val parts = (event.data as String).split("\t", limit = 2)
        println("onMessage parts=$parts")
        val (apiName, request) = parts
        GlobalScope.launch {
            val response = contextHandler.dispatch(apiName, request)
            println("response=$response")
            socket.send(response)
        }
    }
    socket.onmessage = ::onMessage

}

fun Node.sayHello() {
    append {
        div {
            +"Hello from JS"
        }
    }
}