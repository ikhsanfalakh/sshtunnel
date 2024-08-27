package org.programmerplanet.sshtunnel.ui

import org.apache.commons.logging.LogFactory
import org.programmerplanet.sshtunnel.model.Session

/**
 *
 * @author [Mulya Agung](agungm@outlook.com)
 */
object SessionConnectionMonitorFactory {
    private const val DEF_MONITOR_INTERVAL = 10000
    private val log = LogFactory.getLog(SessionConnectionMonitorFactory::class.java)

    private val lock = Any()
    private var thread: Thread? = null
    private val sessionConnectionMonitor = SessionConnectionMonitor(DEF_MONITOR_INTERVAL)

    fun startMonitor() {
        synchronized(lock) {
            if (thread == null) {
                thread = Thread(sessionConnectionMonitor).apply {
                    start()
                }
            }
        }
    }

    fun stopMonitor() {
        synchronized(lock) {
            thread?.let {
                sessionConnectionMonitor.setThreadStopped(true)
                thread = null
                if (log.isWarnEnabled) {
                    log.warn("Connection monitor is stopped.")
                }
            }
        }
    }

    fun addSession(name: String?, session: Session?) {
        sessionConnectionMonitor.addSession(name, session)
    }

    fun removeSession(name: String?) {
        sessionConnectionMonitor.removeSession(name)
    }

    fun setSshTunnelComposite(sshTunnelComposite: SshTunnelComposite?) {
        sessionConnectionMonitor.setSshTunnelComposite(sshTunnelComposite)
    }
}
