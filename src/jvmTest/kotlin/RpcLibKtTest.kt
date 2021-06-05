import kotlinx.coroutines.test.runBlockingTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import rpc.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RpcLibKtTest {


    //https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
    @Test
    fun test0() = runBlockingTest { // a coroutine with an extra test control
        val request1 = ApiRequest1("ciao")
        val response = send(request1) { apiName, it ->
            assertEquals(ApiRequest1::class.simpleName ?: error("no class name"), apiName)
            println(it)
            assertTrue(it.contains("ciao"))
            val re = ApiResponse1("world")
            val resu = Json { }.encodeToString(re)
            println(resu)
            resu
        }
        assertEquals("world", response.pong)
    }

    @Test
    fun test1() = runBlockingTest {
        val handlers = Handlers()
        handlers.register { r: ApiRequest1 -> ApiResponse1("${r.ping}-from-handler") }
        handlers.register { r: ApiRequestSum -> ApiResponseSum(r.a + r.b) }

        assertTrue(
            handlers.dispatch(
                ApiRequest1::class.simpleName ?: error("no class.simpleName"),
                Json { }.encodeToString(ApiRequest1("hello"))
            ).contains("hello-from-handler")
        )
        assertTrue(
            handlers.dispatch(
                ApiRequestSum::class.simpleName ?: error("no class.simpleName"),
                Json { }.encodeToString(ApiRequestSum(1111, 2222))
            ).contains("3333")
        )

    }

    @Test
    fun testContext() = runBlockingTest {
        class MockContext(val name: String)


        val handlers = ContextHandlers<MockContext>()
        handlers.register { r: ApiRequestSum, context: MockContext ->
            assertEquals("foo", context.name)
            ApiResponseSum(r.a + r.b)
        }


        assertTrue(
            handlers.dispatch(
                ApiRequestSum::class.simpleName ?: error("no class.simpleName"),
                Json { }.encodeToString(ApiRequestSum(1111, 2222)),
                MockContext("foo")
            ).contains("3333")
        )

    }

    @Test
    fun testContext_withoutContext_shouldWorkAnyway() = runBlockingTest {
        class MockContext(val name: String)


        val handlers = ContextHandlers<MockContext>()
        handlers.register { r: ApiRequestSum ->
            ApiResponseSum(r.a + r.b)
        }


        assertTrue(
            handlers.dispatch(
                ApiRequestSum::class.simpleName ?: error("no class.simpleName"),
                Json { }.encodeToString(ApiRequestSum(1111, 2222)),
                MockContext("foo")
            ).contains("3333")
        )

        assertTrue(
            handlers.dispatch(
                ApiRequestSum::class.simpleName ?: error("no class.simpleName"),
                Json { }.encodeToString(ApiRequestSum(3333, 2222))
            ).contains("5555")
        )

    }

}