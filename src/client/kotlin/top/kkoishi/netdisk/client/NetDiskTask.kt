package top.kkoishi.netdisk.client

import top.kkoishi.netdisk.client.Constants.EXIT_ABNORMAL
import top.kkoishi.netdisk.client.Constants.EXIT_CMDERR
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.jvm.Throws

/**
 * This class is used to process the arguments in GNU/UNIX style, and run the NetDisk.
 *
 * @author KKoishi_
 */
class NetDiskTask {
    private val context: Context = Context()
    private val recognizedOptions: Array<Option> = arrayOf(
        object : Option(false, "-h", "-?", "--help") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.help = true
            }
        },
        object : Option(false, "-v", "--version") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.version = true
            }
        },
        object : Option(false, "-r", "--recursive") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.recursive = true
            }
        },
        object : Option(true, "-a", "--address") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.address = arg
            }
        },
        object : Option(true, "-p", "--password") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.password = arg
            }
        },
        object : Option(false, "-u", "--upload") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.upload = true
            }
        },
        object : Option(false, "-d", "--download") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.upload = false
            }
        },
        object : Option(true, "-t", "--target") {
            override fun process(task: NetDiskTask, opt: String, arg: String) {
                task.options.target = arg
            }
        }
    )
    private val paths = ArrayDeque<String>(4)
    private val options: Options = Options.instance(context)

    fun processOptions(args: Array<String>): Int {
        try {
            if (args.isEmpty()) {
                options.nogui = false
                return run()
            }
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

    @Throws(BadArgs::class)
    private fun processOptions0(args: Array<String>) {
        val rest = args.iterator()

        while (rest.hasNext()) {
            val arg = rest.next()
            if (arg.startsWith('-') || arg.startsWith("--")) {
                processOption(arg, rest)
            } else {
                if (options.upload) {
                    val rs = ensurePath(arg)
                    if (rs is Path)
                        paths.addLast(rs.toString())
                    else
                        throw rs as BadArgs
                } else
                    paths.addLast(arg)
            }
        }

        if (paths.isEmpty() && !(options.help || options.version))
            throw BadArgs("err.no.paths.specified")

        if (options.help)
            showHelp()

        if (options.version)
            showVersion()
    }

    @Throws(BadArgs::class)
    private fun processOption(name: String, rest: Iterator<String>) {
        for (option in recognizedOptions) {
            if (option.matches(name)) {
                if (option.hasArg) {
                    if (rest.hasNext())
                        option.process(this, name, rest.next())
                    else
                        throw BadArgs("err.missing.arg", name).showUsage(true)
                } else
                    option.process(this, name, "")
                return
            }
        }
    }

    @Throws(BadArgs::class)
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

    private fun showHelp() {
        TODO()
    }

    private fun showVersion() {
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
    private abstract class Option(val hasArg: Boolean, vararg aliases: String) {
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

        abstract fun process(task: NetDiskTask, opt: String, arg: String)
    }
}