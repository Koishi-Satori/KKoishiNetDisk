package top.kkoishi.netdisk.client

import top.kkoishi.netdisk.client.Constants.EXIT_ABNORMAL
import top.kkoishi.netdisk.client.Constants.EXIT_CMDERR
import top.kkoishi.netdisk.client.Constants.EXIT_ERROR
import top.kkoishi.netdisk.client.Constants.EXIT_OK
import top.kkoishi.netdisk.client.io.Messages
import top.kkoishi.netdisk.client.swing.NetDiskWindow
import java.io.PrintWriter
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.io.path.exists
import kotlin.jvm.Throws

/**
 * This class is used to process the arguments in GNU/UNIX style, and run the NetDisk.
 *
 * @author KKoishi_
 */
class NetDiskTask {
    private val context: Context = Context()

    init {
        context[NetDiskTask::class.java] = this
    }

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
    private val messages: Messages = Messages.instance(context)
    private val log = PrintWriter(System.out, true)

    /**
     * Process the program arguments and return the exiting state.
     *
     * @param args the program arguments
     * @return state
     * @see Constants
     */
    fun processOptions(args: Array<String>): Int {
        try {
            if (args.isEmpty()) {
                options.nogui = false
                return run()
            }
            processOptions0(args)

            return run()
        } catch (ba: BadArgs) {
            reportError(ba.key, *ba.args)
            if (ba.showUsage) {
                printLines(getMessage("main.usage.summary", "knetdisk"))
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
            reportError("err.internal.error", *eArgs)
            return EXIT_ABNORMAL
        } catch (re: RuntimeError) {
            val eArgs: Array<Any>
            if (re.cause == null)
                eArgs = re.args
            else {
                with(re.args) {
                    eArgs = Array(size + 1) {}
                    eArgs[0] = re.cause
                    System.arraycopy(this, 0, eArgs, 1, size)
                }
            }
            reportError("err.runtime.error", *eArgs)
            return EXIT_ERROR
        } catch (t: Throwable) {
            t.printStackTrace()
            reportError("err.generic", t)
            return EXIT_ERROR
        } finally {
            log.flush()
        }
    }

    @Throws(InternalError::class, RuntimeError::class)
    private fun run(): Int {
        //TODO: Finish this
        if (options.nogui)
            NetDisk(context).run()
        else
            NetDiskWindow(context, getMessage("gui.title")).run()
        return EXIT_OK
    }

    /**
     * Process options.
     *
     * @param args the program arguments
     * @see Option
     * @see Options
     */
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

    private fun reportError(key: String, vararg args: Any) {
        printLines(getMessage(key, *args))
    }

    private fun getMessage(key: String, vararg args: Any): String {
        return messages.getMessage(key, *args)
    }

    private fun showHelp() {
        printLines(getMessage("main.opt.all", "knetdisk"))
        for (option in recognizedOptions) {
            val name = option.aliases.last().replace(regex = Regex("^-+"), replacement = "")
            printLines(getMessage("main.opt.$name", option.aliases.joinToString()))
        }
    }

    private fun showVersion() {
        TODO()
    }

    private fun printLines(msg: String) {
        log.println(msg.replace("\\n", Constants.nl).replace("\\t", "\t"))
    }

    @Suppress("UNCHECKED_CAST")
    private inner class BadArgs(val key: String, vararg args: Any) : Exception(getMessage(key, *args)) {
        val args: Array<Any> = args as Array<Any>
        var showUsage = false

        fun showUsage(b: Boolean): BadArgs {
            showUsage = b
            return this
        }
    }

    /**
     * A class used to match arguments and process them.
     *
     * @author KKoishi_
     */
    @Suppress("UNCHECKED_CAST")
    private abstract class Option(val hasArg: Boolean, vararg aliases: String) {
        val aliases: Array<String> = aliases as Array<String>

        /**
         * This method checks if the given argument meets one of these in args.
         *
         * @param arg the input argument
         * @return if meets.
         */
        fun matches(arg: String): Boolean {
            for (a in aliases) {
                if (a == arg)
                    return true
            }
            return false
        }

        /**
         * Process the arguments.
         *
         * @param task NetDiskTask instance
         * @param opt the option
         * @param arg the extensional argument
         */
        abstract fun process(task: NetDiskTask, opt: String, arg: String)
    }
}