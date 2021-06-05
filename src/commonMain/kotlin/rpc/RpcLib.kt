package rpc

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ApiConf {
    fun baseUrl(apiName: String) = "api1/$apiName"
}


interface Request<T : Any>


suspend inline fun <reified Req : Request<Resp>, reified Resp : Any> send(
    request: Req,
    dispatcher: suspend (String, String) -> String
): Resp {
    val requestJson = Json { }.encodeToString(request)
    val responseJson = dispatcher(Req::class.simpleName ?: error("no class name"), requestJson)
    val response = Json { }.decodeFromString<Resp>(responseJson)
    return response
}

data class SerializerPair(val serializer: (Any) -> String, val deserializer: (String) -> Any)

open class Handlers {
    data class Handler(
        val requestSerializer: SerializerPair,
        val responseSerializer: SerializerPair,
        val handler: suspend (Any) -> Any
    )

    val handlers = mutableMapOf<String, Handler>()

    inline fun <reified Req : Request<Resp>, reified Resp : Any> register(noinline function: suspend (Req) -> Resp) {
        val handlerName = Req::class.simpleName ?: error("no name")
        handlers[handlerName] = Handler(
            requestSerializer = serializers<Req>(),
            responseSerializer = serializers<Resp>(),
            handler = function as suspend (Any) -> Any
        )
    }


    inline fun <reified T> serializers(): SerializerPair = SerializerPair(
        serializer = { instance: T -> Json { }.encodeToString(instance) } as (Any) -> String,
        deserializer = { json: String -> Json { }.decodeFromString<T>(json) } as (String) -> Any
    )

    suspend fun dispatch(simpleName: String, payload: String): String {
        val handler = handlers[simpleName] ?: error("no handler for $simpleName")
        handler.apply {
            val r = requestSerializer.deserializer(payload)
            val res = handler(r)
            return responseSerializer.serializer(res)
        }
    }
}

class ContextHandlers<Context> : Handlers() {

    data class ContextHandler<Context>(
        val requestSerializer: SerializerPair,
        val responseSerializer: SerializerPair,
        val handler: suspend (Any, Context) -> Any
    )

    val contextHandlers = mutableMapOf<String, ContextHandler<Context>>()

    inline fun <reified Req : Request<Resp>, reified Resp : Any> register(
        noinline function: suspend (Req, Context) -> Resp
    ) {
        val handlerName = Req::class.simpleName ?: error("no name")
        contextHandlers[handlerName] = ContextHandler(
            requestSerializer = serializers<Req>(),
            responseSerializer = serializers<Resp>(),
            handler = function as suspend (Any, Context) -> Any
        )
    }

    suspend fun dispatch(simpleName: String, payload: String, context: Context): String {

        val handler = contextHandlers[simpleName] ?: return super.dispatch(simpleName, payload)

        handler.apply {
            val r = requestSerializer.deserializer(payload)
            val res = handler(r, context)
            return responseSerializer.serializer(res)
        }
    }

}