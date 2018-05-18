package ru.nchernetsov.test.sbertech.client.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.util.stream.Collectors

object ClientUtils {
    private val LOG: Logger = LoggerFactory.getLogger(ClientUtils::class.java)

    /**
     * Находим список MAC-адресов данного хоста. Будем считать его уникальными именем клиента
     */
    fun getMacAddress(): String {
        try {
            val macList = ArrayList<String>()

            val networks = NetworkInterface.getNetworkInterfaces()

            while (networks.hasMoreElements()) {
                val network = networks.nextElement()
                val mac = network.hardwareAddress

                if (mac != null) {
                    val sb = StringBuilder()
                    for (i in mac.indices) {
                        sb.append(String.format("%02X%s", mac[i], if (i < mac.size - 1) "-" else ""))
                    }
                    macList.add(sb.toString())
                }
            }

            return macList.stream().collect(Collectors.joining("|"))
        } catch (e: SocketException) {
            LOG.error(e.message)
        }

        return ""
    }
}
