/*
 * Copyright 2009 Joseph Fifield
 * Copyright 2022 Mulya Agung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.programmerplanet.sshtunnel.model

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Logger
import com.jcraft.jsch.UserInfo
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.eclipse.swt.widgets.Shell
import org.programmerplanet.sshtunnel.ui.DefaultUserInfo
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter

/**
 * Responsible for connecting and disconnecting ssh connections and the
 * underlying tunnels.
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 * @author [Mulya Agung](agungm@outlook.com)
 */
class ConnectionManager {
    internal sealed class TunnelUpdateState {
        data object START : TunnelUpdateState()
        data object STOP : TunnelUpdateState()
        data object CHANGE : TunnelUpdateState()
        companion object {
            fun values(): Array<TunnelUpdateState> {
                return arrayOf(START, STOP, CHANGE)
            }
        }
    }

    companion object {
        private val log: Log = LogFactory.getLog(ConnectionManager::class.java)

        private const val TIMEOUT = 20000
        private const val KEEP_ALIVE_INTERVAL = 40000
        private const val DEF_CIPHERS = "aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-cbc,aes128-ctr"

        private val serverSocketFactory = TrackedServerSocketFactory()

        private val connections: MutableMap<Session, com.jcraft.jsch.Session?> = HashMap()

        fun startTunnelIfSessionConnected(session: Session, tunnel: Tunnel) {
            updateTunnelIfSessionConnected(session, TunnelUpdateState.START, tunnel, null)
        }

        fun stopTunnelIfSessionConnected(session: Session, tunnel: Tunnel) {
            updateTunnelIfSessionConnected(session, TunnelUpdateState.STOP, tunnel, null)
        }

        fun changeTunnelIfSessionConnected(session: Session, tunnel: Tunnel, prevTunnel: Tunnel): Int {
            return updateTunnelIfSessionConnected(session, TunnelUpdateState.CHANGE, tunnel, prevTunnel)
        }

        private fun updateTunnelIfSessionConnected(
            session: Session,
            state: TunnelUpdateState,
            tunnel: Tunnel,
            prevTunnel: Tunnel?
        ): Int {
            var status = 0
            val jschSession = connections[session]
            if (jschSession != null && jschSession.isConnected) {
                try {
                    when (state) {
                        TunnelUpdateState.START -> startTunnel(jschSession, tunnel)
                        TunnelUpdateState.STOP -> stopTunnel(jschSession, tunnel)
                        else -> {
                            stopTunnel(jschSession, prevTunnel)
                            startTunnel(jschSession, tunnel)
                        }
                    }
                } catch (e: JSchException) {
                    status = -1
                    log.error(e)
                }
            }
            return status
        }

        @Throws(ConnectionException::class)
        fun connect(session: Session, parent: Shell?) {
            log.info("Connecting session: $session")
            clearTunnelExceptions(session)
            var jschSession = connections[session]
            try {
                if (jschSession == null) {
                    val jsch = JSch()
                    val knownHosts = knownHostsFile
                    jsch.setKnownHosts(knownHosts.absolutePath)

                    if (session.identityPath != null && !session.identityPath!!.trim { it <= ' ' }.isEmpty()) {
                        try {
                            if (session.passPhrase != null && !session.passPhrase!!.trim { it <= ' ' }.isEmpty()) {
                                jsch.addIdentity(session.identityPath, session.passPhrase)
                            } else {
                                jsch.addIdentity(session.identityPath)
                            }
                        } catch (e: JSchException) {
                            // Jsch does not support newer format, you may convert the key to the pem format:
                            // ssh-keygen -p -f key_file -m pem -P passphrase -N passphrase
                            throw ConnectionException(e)
                        }
                    }
                    jschSession = jsch.getSession(session.username, session.hostname, session.port)


                    // Set debug logger if set
                    if (session.debugLogPath != null && !session.debugLogPath!!.trim { it <= ' ' }.isEmpty()) {
                        jschSession.setLogger(
                            SshLogger(
                                (session.debugLogPath
                                        + File.separator + "sshtunnelng-" + session.sessionName + ".log")
                            )
                        )
                    }
                }
                val userInfo: UserInfo =
                    if (session.password != null && !session.password!!.trim { it <= ' ' }.isEmpty()) {
                        DefaultUserInfo(parent, session.password)
                    } else {
                        DefaultUserInfo(parent)
                    }

                jschSession!!.userInfo = userInfo
                jschSession.serverAliveInterval = KEEP_ALIVE_INTERVAL
                jschSession.serverAliveCountMax = 2

                if (session.ciphers != null && !session.ciphers!!.isEmpty()) {
                    // Set ciphers to use aes128-gcm if possible, as it is fast on many systems
                    jschSession.setConfig("cipher.s2c", session.ciphers + "," + DEF_CIPHERS)
                    jschSession.setConfig("cipher.c2s", session.ciphers + "," + DEF_CIPHERS)
                    jschSession.setConfig("CheckCiphers", session.ciphers)
                }

                if (session.isCompressed) {
                    jschSession.setConfig("compression.s2c", "zlib@openssh.com,zlib,none")
                    jschSession.setConfig("compression.c2s", "zlib@openssh.com,zlib,none")
                    //jschSession.setConfig("compression_level", "9");
                }

                jschSession.connect(TIMEOUT)

                startTunnels(session, jschSession)
            } catch (e: JSchException) {
                Objects.requireNonNull(jschSession)?.disconnect()
                throw ConnectionException(e)
            } catch (e: IOException) {
                Objects.requireNonNull(jschSession)?.disconnect()
                throw ConnectionException(e)
            }
            connections[session] = jschSession
        }

        private val knownHostsFile: File
            get() {
                val userHome = System.getProperty("user.home")
                var f = File(userHome)
                f = File(f, ".ssh")
                f = File(f, "known_hosts")
                return f
            }

        private fun startTunnels(session: Session, jschSession: com.jcraft.jsch.Session) {
            for (tunnel in session.tunnels) {
                try {
                    startTunnel(jschSession, tunnel)
                } catch (e: Exception) {
                    tunnel.exception = e
                    log.error("Error starting tunnel: $tunnel", e)
                }
            }
        }

        @Throws(JSchException::class)
        private fun startTunnel(jschSession: com.jcraft.jsch.Session, tunnel: Tunnel) {
            if (tunnel.local) {
                //jschSession.setPortForwardingL(tunnel.getLocalAddress(), tunnel.getLocalPort(), tunnel.getRemoteAddress(), tunnel.getRemotePort());
                jschSession.setPortForwardingL(
                    tunnel.localAddress,
                    tunnel.localPort, tunnel.remoteAddress,
                    tunnel.remotePort, serverSocketFactory
                )
            } else {
                jschSession.setPortForwardingR(
                    tunnel.remoteAddress,
                    tunnel.remotePort,
                    tunnel.localAddress,
                    tunnel.localPort
                )
            }
        }


        fun disconnect(session: Session) {
            log.info("Disconnecting session: $session")
            clearTunnelExceptions(session)
            val jschSession = connections[session]
            if (jschSession != null) {
                stopTunnels(session, jschSession)
                jschSession.disconnect()
            }
            connections.remove(session)
        }

        private fun stopTunnels(session: Session, jschSession: com.jcraft.jsch.Session) {
            for (tunnel in session.tunnels) {
                try {
                    stopTunnel(jschSession, tunnel)
                } catch (e: Exception) {
                    log.error("Error stopping tunnel: $tunnel", e)
                }
            }
        }

        @Throws(JSchException::class)
        private fun stopTunnel(jschSession: com.jcraft.jsch.Session, tunnel: Tunnel?) {
            if (tunnel != null) {
                if (tunnel.local) {
                    jschSession.delPortForwardingL(tunnel.localAddress, tunnel.localPort)
                    serverSocketFactory.closeSocket(tunnel.localAddress, tunnel.localPort)
                } else {
                    jschSession.delPortForwardingR(tunnel.remotePort)
                }
            }
        }

        private fun clearTunnelExceptions(session: Session) {
            for (tunnel in session.tunnels) {
                tunnel.exception = null
            }
        }

        fun isConnected(session: Session): Boolean {
            val jschSession = connections[session]
            return jschSession != null && jschSession.isConnected
        }

    }
}

internal class SshLogger(filePath: String) : Logger {
    private val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger(SshLogger::class.java.simpleName)

    init {
        val fh = FileHandler(filePath)
        val formatter = SimpleFormatter()
        fh.formatter = formatter
        logger.addHandler(fh)
    }


    override fun isEnabled(level: Int): Boolean {
        return true
    }

    override fun log(level: Int, message: String) {
        when (level) {
            Logger.INFO -> logger.info(message)
            Logger.WARN -> logger.warning(message)
            Logger.ERROR, Logger.FATAL -> logger.severe(message)
            else -> {}
        }
    }

}


