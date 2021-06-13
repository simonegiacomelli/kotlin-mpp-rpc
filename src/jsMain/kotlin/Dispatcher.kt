import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import rpc.ApiConf
import rpc.Request
import rpc.send

object Api {
    suspend inline fun <reified Req : Request<Resp>, reified Resp : Any> send(
        request: Req
    ): Resp {
        return send(request, ::dispatcher)
    }
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