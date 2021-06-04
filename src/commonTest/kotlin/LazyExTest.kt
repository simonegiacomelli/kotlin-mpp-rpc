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

class LazyEx<out T>(private var initializer: () -> T) : Lazy<T> {

    private var wrap = Wrap()
    override val value: T get() = wrap.lazy.value
    override fun isInitialized() = wrap.lazy.isInitialized()
    override fun toString() = wrap.lazy.toString()
    fun invalidate() {
        wrap = Wrap()
    } // create a new Wrap object

    private inner class Wrap {
        val lazy = lazy(initializer)
    }
}