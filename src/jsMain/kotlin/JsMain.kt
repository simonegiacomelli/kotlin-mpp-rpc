import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.Node
import rpc.ApiRequestSum

fun main() {
    window.onload = { document.body?.sayHello() }

    GlobalScope.launch {
        testApi()
    }

}

suspend fun testApi() {
    console.log("Using api rpc")
    val response = Api.send(ApiRequestSum(5, 7))
    console.log("sum is: " + response.sum)
}

fun Node.sayHello() {
    append {
        div {
            +"Hello from JS"
        }
    }
}