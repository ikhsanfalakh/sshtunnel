package org.programmerplanet.sshtunnel.ui

import io.github.oshai.kotlinlogging.KotlinLogging
import org.programmerplanet.sshtunnel.model.Session

private val logger = KotlinLogging.logger {}

/**
 *
 * @author [Mulya Agung](agungm@outlook.com)
 */
object SessionConnectionMonitorFactory {
    private const val DEF_MONITOR_INTERVAL = 10000

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
                if (logger.isWarnEnabled()) {
                    logger.warn {"Connection monitor is stopped."}
                }
            }
        }
    }

    fun addSession(name: String, session: Session) {
        sessionConnectionMonitor.addSession(name, session)
    }

    fun removeSession(name: String) {
        sessionConnectionMonitor.removeSession(name)
    }

    fun setSshTunnelComposite(applicationComposite: ApplicationComposite) {
        sessionConnectionMonitor.setSshTunnelComposite(applicationComposite)
    }
}
