import top.kkoishi.netdisk.client.NetDiskTask
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(NetDiskTask().processOptions(args))
}