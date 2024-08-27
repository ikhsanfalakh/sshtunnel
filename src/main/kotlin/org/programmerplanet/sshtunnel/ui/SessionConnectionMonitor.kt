package org.programmerplanet.sshtunnel.ui

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.eclipse.swt.widgets.Display
import org.programmerplanet.sshtunnel.model.ConnectionManager.Companion.disconnect
import org.programmerplanet.sshtunnel.model.ConnectionManager.Companion.isConnected
import org.programmerplanet.sshtunnel.model.Session
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author [Mulya Agung](agungm@outlook.com)
 */
class SessionConnectionMonitor(monitorInterval: Int) : Runnable {
    private var threadStopped: Boolean
    private val monitorInterval: Int
    private val sessions: MutableMap<String, Session> =
        ConcurrentHashMap()
    private var sshTunnelComposite: SshTunnelComposite? = null

    init {
        threadStopped = false
        this.monitorInterval = monitorInterval
    }

    fun setThreadStopped(threadStopped: Boolean) {
        this.threadStopped = threadStopped
    }

    override fun run() {
        this.threadStopped = false
        if (log.isWarnEnabled) {
            log.warn("Connection monitor is now running..")
        }
        while (!threadStopped) {
            var anyRemoved = false
            val it: MutableIterator<Map.Entry<String, Session>> = sessions.entries.iterator()
            while (it.hasNext()) {
                val entry: Map.Entry<String, Session> = it.next()
                if (!isConnected(entry.value)) {
                    disconnect(entry.value)
                    if (log.isWarnEnabled) {
                        log.warn("Session " + entry.key + " has disconnected.")
                    }
                    if (sshTunnelComposite != null) {
                        val s = entry.value
                        Display.getDefault().asyncExec {
                            sshTunnelComposite!!.showDisconnectedMessage(
                                s
                            )
                        }
                    }
                    it.remove()
                    if (!anyRemoved) anyRemoved = true
                }
            }
            if (sshTunnelComposite != null && anyRemoved) {
                Display.getDefault().asyncExec { sshTunnelComposite!!.connectionStatusChanged() }
            }
            try {
                Thread.sleep(monitorInterval.toLong())
            } catch (e: InterruptedException) {
                log.error(e)
            }
        }
    }

    fun addSession(name: String, session: Session) {
        sessions[name] = session
    }

    fun removeSession(name: String) {
        sessions.remove(name)
    }

    fun setSshTunnelComposite(sshTunnelComposite: SshTunnelComposite?) {
        this.sshTunnelComposite = sshTunnelComposite
    }

    companion object {
        private val log: Log = LogFactory.getLog(
            SessionConnectionMonitor::class.java
        )
    }
}
