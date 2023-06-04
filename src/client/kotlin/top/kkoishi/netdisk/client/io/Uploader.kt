package top.kkoishi.netdisk.client.io

import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket

class Uploader(address: String, port: Int, private val files: Array<RandomAccessFile>) {
    private val socket: Socket = Socket()

    init {
        socket.soTimeout = 15000
        socket.connect(InetSocketAddress(address, port))
    }
}