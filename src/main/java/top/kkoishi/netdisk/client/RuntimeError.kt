package top.kkoishi.netdisk.client

@Suppress("UNCHECKED_CAST")
class RuntimeError: Error {
    val args: Array<Any>
    constructor(t: Throwable, vararg args: Any): super("Runtime Error", t) {
        this.args = args as Array<Any>
    }

    constructor(vararg args: Any): super("Runtime Error") {
        this.args = args as Array<Any>
    }
}