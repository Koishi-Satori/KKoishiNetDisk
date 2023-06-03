package top.kkoishi.netdisk.client

class Options private constructor(context: Context) {

    init {
        context[Options::class.java] = this
    }

    companion object {
        @JvmStatic
        fun instance(context: Context): Options {
            var instance = context[Options::class.java]
            if (instance == null)
                instance = Options(context)
            return instance
        }
    }

    var nogui: Boolean = true
    var help: Boolean = false
    var version: Boolean = false
    var recursive: Boolean = false
    var upload: Boolean = false
    var address: String = ""
    var password: String = ""
    var target: String = ""
}