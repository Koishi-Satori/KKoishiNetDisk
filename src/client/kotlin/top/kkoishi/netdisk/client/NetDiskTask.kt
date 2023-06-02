package top.kkoishi.netdisk.client

import top.kkoishi.netdisk.client.Constants.EXIT_ABNORMAL
import top.kkoishi.netdisk.client.Constants.EXIT_CMDERR
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * This class is used to process the arguments in GNU/UNIX style, and run the NetDisk.
 *
 * @author KKoishi_
 */
class NetDiskTask {
    private val recorgnizedOptions: Array<Option> = arrayOf(

    )
    private val paths = ArrayDeque<Path>(4)

    fun processOptions(args: Array<String>): Int {
        try {
            processOptions0(args)

            return run()
        } catch (ba: BadArgs) {
            reportError(ba.key, ba.args)
            if (ba.showUsage) {
                TODO()
            }
            return EXIT_CMDERR
        } catch (e: InternalError) {
            val eArgs: Array<Any>
            if (e.cause == null)
                eArgs = e.args
            else {
                with(e.args) {
                    eArgs = Array(size + 1) {}
                    eArgs[0] = e.cause
                    System.arraycopy(this, 0, eArgs, 1, size)
                }
            }
            reportError("err.internal.error", eArgs)
            return EXIT_ABNORMAL
        }
    }

    private fun run(): Int {
        TODO()
    }

    private fun processOptions0(args: Array<String>) {
        val rest = args.iterator()
        val noArgs = !rest.hasNext()

        while (rest.hasNext()) {
            val arg = rest.next()
            if (arg.startsWith('-') || arg.startsWith("--")) {
                processOption(arg, rest)
            } else {
                val rs = ensurePath(arg)
                if (rs is Path)
                    paths.addLast(rs)
                else
                    throw rs as BadArgs
            }
        }
    }

    private fun processOption(arg: String, rest: Iterator<String>) {

    }

    private fun ensurePath(p: String): Any {
        if (p.isEmpty())
            return BadArgs("io.path.existence", p)
        return try {
            val path = Path.of(p)
            if (path.exists())
                return path
            BadArgs("io.path.existence", p)
        } catch (e: InvalidPathException) {
            BadArgs("io.path.invalid", p)
        }
    }

    private fun reportError(key: String, args: Array<Any>) {
        TODO()
    }

    private fun getMessage(key: String, vararg args: Any): String {
        TODO()
    }

    @Suppress("UNCHECKED_CAST")
    private inner class BadArgs(val key: String, vararg args: Any) : Exception(getMessage(key, args)) {
        val args: Array<Any> = args as Array<Any>
        var showUsage = false

        fun showUsage(b: Boolean): BadArgs {
            showUsage = b
            return this
        }
    }

    @Suppress("UNCHECKED_CAST")
    private abstract inner class Option(val hasArg: Boolean, vararg aliases: String) {
        private val aliases: Array<String> = aliases as Array<String>

        /**
         * This method checks if the given argument meets one of these in args.
         *
         * @return if meets.
         */
        fun matches(arg: String): Boolean {
            for (a in aliases) {
                if (a == arg)
                    return true
            }
            return false
        }

        abstract fun hasAdditionalOption(): Boolean
        abstract fun process(task: NetDiskTask, arg: String, other: String)
    }
}