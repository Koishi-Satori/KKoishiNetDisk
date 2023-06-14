package top.kkoishi.netdisk.client.io

import top.kkoishi.netdisk.client.InternalError
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import kotlin.jvm.Throws

class DownloadThread constructor(
    private var start: Long,
    private val end: Long,
    private val cout: RandomAccessFile,
    private val cin: InputStream,
    private val bufferSize: Int,
) : Runnable {
    private var count: ThreadLocal<Int> = ThreadLocal()
    private var state: ThreadLocal<Int> = ThreadLocal()

    init {
        state.set(STATE_INIT)
        count.set(0)
    }

    @Throws(InternalError::class)
    override fun run() {
        count.set(count.get() + 1)
        state.set(STATE_PROCESSING)
        try {
            cout.seek(start)
            val buffer = ByteArray(bufferSize)
            var len: Int
            while (true) {
                len = cin.read(buffer)
                start += len
                if (len == -1 && start <= end)
                    break
                cout.write(buffer, 0, len)
            }
            state.set(STATE_END)
        } catch (e: IOException) {
            state.set(STATE_END_ABNORMAL)
            throw InternalError(e)
        } finally {
            cout.close()
            cin.close()
        }
    }

    fun state() = state.get()

    fun end() = count.get() >= 5

    companion object {
        const val STATE_INIT = 0
        const val STATE_PROCESSING = 1
        const val STATE_END = 2
        const val STATE_END_ABNORMAL = 3
    }
}