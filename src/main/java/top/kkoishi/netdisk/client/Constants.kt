package top.kkoishi.netdisk.client

object Constants {
    const val EXIT_OK = 0
    const val EXIT_CMDERR = 1
    const val EXIT_ERROR = 2
    const val EXIT_ABNORMAL = 3

    @JvmStatic
    val nl = System.getProperty("line.separator")

    /**
     * 64MB.
     */
    const val SPILT_SIZE = 1024 * 1024 * 64L

    @JvmStatic
    val FLAG_DOWNLOAD = byteArrayOf(0x01, 0x01)
}