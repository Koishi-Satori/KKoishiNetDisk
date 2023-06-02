package top.kkoishi.netdisk.client

@Suppress("UNCHECKED_CAST")
class InternalError: Error {
    val args: Array<Any>
    constructor(t: Throwable, vararg args: Any): super("Internal Error", t) {
        this.args = args as Array<Any>
    }

    constructor(vararg args: Any): super("Internal Error") {
        this.args = args as Array<Any>
    }
}