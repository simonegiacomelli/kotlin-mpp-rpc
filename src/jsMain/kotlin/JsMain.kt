import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.dom.append
import md.ApiConf
import md.ApiRequestSum
import md.send
import org.w3c.dom.Node
import org.w3c.fetch.RequestInit

fun main() {
    window.onload = { document.body?.sayHello() }

    GlobalScope.launch {
        testApi()
    }

}

suspend fun testApi() {
    val response = send(ApiRequestSum(5, 7), ::dispatcher)
    console.log(response)
}

suspend fun dispatcher(apiName: String, payload: String): String {
    val url = "http://localhost:8080/" + ApiConf.baseUrl(apiName)
    val request = RequestInit()
    request.method = "POST"
    request.body = payload
    val resp = window.fetch(url, request).await()
    val response = resp.text().await().split("\n\n", limit = 2)
    if (response[0] == "success=1")
        return response[1]
    error(response[1])
}

fun Node.sayHello() {
    append {
        div {
            +"Hello from JS"
        }
    }
}