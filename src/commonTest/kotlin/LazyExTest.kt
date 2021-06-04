import kotlin.test.Test
import kotlin.test.assertEquals

class LazyExTest {
    @Test
    fun test1() {
        var counter = 0
        val target = lazyEx { ++counter }
        val prop by target
        assertEquals(1, prop)
        assertEquals(1, prop)
        target.invalidate()
        assertEquals(2, prop)
    }


}

fun <T> lazyEx(initializer: () -> T): LazyEx<T> = LazyEx(initializer)

class LazyEx<T>(private var initializer: () -> T) : Lazy<T> {

    private var wrap = lazy(initializer)
    override val value: T get() = wrap.value
    override fun isInitialized() = wrap.isInitialized()
    override fun toString() = wrap.toString()
    fun invalidate() {
        wrap = lazy(initializer)
    }


}