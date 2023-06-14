package top.kkoishi.netdisk.client.io

import top.kkoishi.netdisk.client.Context
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.jvm.Throws

class FileManager private constructor(context: Context) {

    init {
        context[FileManager::class.java] = this
    }

    companion object {
        @JvmStatic
        @Suppress("SENSELESS_COMPARISON")
        fun instance(context: Context): FileManager {
            var instance = context[FileManager::class.java]
            if (instance == null)
                instance = FileManager(context)
            return instance
        }
    }

    @Throws(IOException::class)
    fun ensurePath(p: String): Path {
        try {
            val path = Path.of(p)
            if (path.exists())
                return path
            return path.createDirectories()
        } catch (e: Exception) {
            throw IOException(e)
        }
    }
}