package top.kkoishi.netdisk.client.io

import top.kkoishi.netdisk.client.*
import top.kkoishi.netdisk.client.Constants.FLAG_DOWNLOAD
import top.kkoishi.netdisk.client.io.DownloadThread.Companion.STATE_END
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws
import top.kkoishi.netdisk.client.io.DownloadThread.Companion.STATE_END_ABNORMAL
import java.util.concurrent.Callable

class Downloader(
    address: String,
    port: Int,
    private val files: Array<String>,
    private val context: Context,
) : Runnable {
    private val socket: Socket = Socket()
    private val threads: ArrayDeque<DownloadThread>
    private val pool: ScheduledThreadPoolExecutor

    init {
        val maxThreads = Options.instance(context).maxThreads
        socket.soTimeout = 15000
        socket.connect(InetSocketAddress(address, port))
        threads = ArrayDeque(maxThreads)
        pool = ScheduledThreadPoolExecutor(maxThreads)
    }

    @Throws(InternalError::class, RuntimeError::class)
    override fun run() {
        try {
            val ins = DataInputStream(socket.getInputStream())
            val out = DataOutputStream(socket.getOutputStream())
            val errBuf = StringBuilder()

            val options = Options.instance(context)
            val maxThreads = options.maxThreads
            val target =
                FileManager.instance(context).ensurePath(options.target).toRealPath().toString()
            val bufferSize = options.bufferSize

            val daemon = Daemon()
            out.write(FLAG_DOWNLOAD)
            for (name in files) {
                out.writeUTF(name)
                val size = ins.readLong()
                val fileName = ins.readUTF()

                if (size == -1L)
                    errBuf.append("File does not exist: ").append(name).append(';')
                else {
                    // the file pointer of RandomAccessFile -> Long type.
                    var spiltSize = Constants.SPILT_SIZE
                    var start: Long = 0
                    var end: Long = spiltSize

                    val threadsCount = size / spiltSize + 1
                    if (threadsCount > maxThreads) {
                        // recalculate how to schedule download threads
                        spiltSize = (size / maxThreads) + 1
                        end = spiltSize
                    }

                    while (end < size) {
                        val dt =
                            DownloadThread(start, end, RandomAccessFile("$target/$fileName", "rw"), ins, bufferSize)
                        threads.addLast(dt)
                        pool.schedule(dt, 0L, TimeUnit.MILLISECONDS)

                        start += spiltSize
                        end += spiltSize
                        if (end > size) {
                            end = size
                        }
                    }

                    while (true) {
                        val call = daemon.call()
                        if (call) {
                            break
                        }
                        if (!call && threads.isEmpty()) {
                            errBuf.append("Failed to download: $name")
                            break
                        }
                    }
                }
            }

            if (errBuf.isNotEmpty())
                throw RuntimeError(errBuf)
        } catch (e: IOException) {
            throw InternalError(e)
        }
    }

    private inner class Daemon: Callable<Boolean> {
        override fun call(): Boolean {
            val ended = ArrayDeque<DownloadThread>(threads.size)
            for (t in threads) {
                if (t.state() == STATE_END_ABNORMAL) {
                    if (t.end()) {
                        // try to shut down
                        threads.clear()
                        pool.shutdownNow()
                        return false
                    } else {
                        // try to restart
                        pool.remove(t)
                        pool.schedule(t, 0L, TimeUnit.MILLISECONDS)
                    }
                } else if (t.state() == STATE_END)
                    ended.addLast(t)
            }
            threads.removeAll(ended)
            return threads.isEmpty()
        }
    }
}