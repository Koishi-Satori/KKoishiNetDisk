package top.kkoishi.netdisk.client

@Suppress("UNCHECKED_CAST")
class Context {
    private val map: MutableMap<Class<*>, Any> = HashMap()

    operator fun <T> get(key: Class<T>): T? {
        return map[key] as T?
    }

    operator fun <T> set(key: Class<T>, value: T) {
        map[key] = value!!
    }
}