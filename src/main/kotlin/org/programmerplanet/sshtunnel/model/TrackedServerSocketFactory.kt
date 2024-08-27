package org.programmerplanet.sshtunnel.model

import com.jcraft.jsch.ServerSocketFactory
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

class TrackedServerSocketFactory : ServerSocketFactory {
    private val socketMap: MutableMap<String, ServerSocket> =
        HashMap()

    @Throws(IOException::class)
    override fun createServerSocket(port: Int, backlog: Int, bindAddr: InetAddress): ServerSocket {
        val serverSocket: ServerSocket = CustomServerSocket(port, backlog, bindAddr)
        val key = "$bindAddr:$port"
        socketMap[key] = serverSocket
        return serverSocket
    }

    fun closeSocket(addr: String?, port: Int) {
        try {
            val bindAddr = InetAddress.getByName(normalize(addr))

            val key = "$bindAddr:$port"
            val socket = socketMap[key]
            if (socket != null) {
                val customSocket = socket as CustomServerSocket
                customSocket.closeTunnelSocket(bindAddr.toString(), port)
                try {
                    customSocket.close()
                } catch (e: IOException) {
                    log.error(e)
                } finally {
                    socketMap.remove(key)
                }
            }
        } catch (e: UnknownHostException) {
            log.error(e)
        }
    }

    companion object {
        private val log: Log = LogFactory.getLog(
            TrackedServerSocketFactory::class.java
        )

        fun normalize(address: String?): String? {
            var initAddress: String? = address
            if (initAddress != null) {
                if (initAddress.isEmpty() || address == "*") initAddress = "0.0.0.0"
                else if (address == "localhost") initAddress = "127.0.0.1"
            }
            return initAddress
        }
    }
}

internal class CustomServerSocket(port: Int, backlog: Int, bindAddr: InetAddress?) :
    ServerSocket(port, backlog, bindAddr) {
    private val tunnelSockets: MutableMap<String, MutableList<Socket>> =
        ConcurrentHashMap()

    @Throws(IOException::class)
    override fun accept(): Socket {
        val socket = super.accept()
        val key = socket.inetAddress.toString() + ":" + socket.localPort

        if (!tunnelSockets.containsKey(key)) tunnelSockets[key] = ArrayList()
        tunnelSockets[key]!!.add(socket)

        return socket
    }

    fun closeTunnelSocket(addr: String, port: Int) {
        val key = "$addr:$port"
        val sockets: List<Socket>? = tunnelSockets[key]
        if (!sockets.isNullOrEmpty()) {
            for (socket in sockets) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    log.error(e)
                }
            }
            tunnelSockets.remove(key)
        }
    }

    companion object {
        private val log: Log = LogFactory.getLog(
            CustomServerSocket::class.java
        )
    }
}
