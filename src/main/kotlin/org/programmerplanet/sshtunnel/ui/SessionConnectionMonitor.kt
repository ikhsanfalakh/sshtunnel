package org.programmerplanet.sshtunnel.ui

import io.github.oshai.kotlinlogging.KotlinLogging
import org.eclipse.swt.widgets.Display
import org.programmerplanet.sshtunnel.model.ConnectionManager
import org.programmerplanet.sshtunnel.model.Session
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 *
 * @author [Mulya Agung](agungm@outlook.com)
 */
class SessionConnectionMonitor(monitorInterval: Int) : Runnable {
    private var threadStopped: Boolean
    private val monitorInterval: Int
    private val sessions: MutableMap<String, Session> =
        ConcurrentHashMap()
    private var applicationComposite: ApplicationComposite? = null

    init {
        threadStopped = false
        this.monitorInterval = monitorInterval
    }

    fun setThreadStopped(threadStopped: Boolean) {
        this.threadStopped = threadStopped
    }

    override fun run() {
        this.threadStopped = false
        if (logger.isWarnEnabled()) {
            logger.warn {"Connection monitor is now running.."}
        }
        while (!threadStopped) {
            var anyRemoved = false
            val it: MutableIterator<Map.Entry<String, Session>> = sessions.entries.iterator()
            while (it.hasNext()) {
                val entry: Map.Entry<String, Session> = it.next()
                if (!ConnectionManager.isConnected(entry.value)) {
                    ConnectionManager.disconnect(entry.value)
                    if (logger.isWarnEnabled()) {
                        logger.warn { "Session " + entry.key + " has disconnected." }
                    }
                    if (applicationComposite != null) {
                        val s = entry.value
                        Display.getDefault().asyncExec {
                            applicationComposite!!.showDisconnectedMessage(
                                s
                            )
                        }
                    }
                    it.remove()
                    if (!anyRemoved) anyRemoved = true
                }
            }
            if (applicationComposite != null && anyRemoved) {
                Display.getDefault().asyncExec { applicationComposite!!.connectionStatusChanged() }
            }
            try {
                Thread.sleep(monitorInterval.toLong())
            } catch (e: InterruptedException) {
                logger.error(e) { "interrupted thread" }
            }
        }
    }

    fun addSession(name: String, session: Session) {
        sessions[name] = session
    }

    fun removeSession(name: String) {
        sessions.remove(name)
    }

    fun setSshTunnelComposite(applicationComposite: ApplicationComposite?) {
        this.applicationComposite = applicationComposite
    }

}
