package org.programmerplanet.sshtunnel.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmerplanet.sshtunnel.model.Session;

/**
 * 
 * @author <a href="agungm@outlook.com">Mulya Agung</a>
 */

public class SessionConnectionMonitorFactory {

	private static final Log log = LogFactory.getLog(SessionConnectionMonitorFactory.class);

	private static final int DEF_MONITOR_INTERVAL = 10000;
	private static final SessionConnectionMonitorFactory INSTANCE = new SessionConnectionMonitorFactory();

	private final Object lock = new Object();
	private Thread thread;
	private final SessionConnectionMonitor sessionConnectionMonitor;

	public SessionConnectionMonitorFactory() {
		this(DEF_MONITOR_INTERVAL);
	}

	public SessionConnectionMonitorFactory(int monitorInterval) {
		this.sessionConnectionMonitor = new SessionConnectionMonitor(monitorInterval);
	}

	public void startMonitor() {
		synchronized (lock) {
			if (thread == null) {
				thread = new Thread(this.sessionConnectionMonitor);
				thread.start();
			}
		}
	}

	public void stopMonitor() {
		synchronized (lock) {
			if (thread != null) {
				this.sessionConnectionMonitor.setThreadStopped(true);
				thread = null;
				
				if (log.isWarnEnabled()) {
					log.warn("Connection monitor is stopped.");
				}
			}
		}
	}

	public void addSession(String name, Session session) {
		this.sessionConnectionMonitor.addSession(name, session);
	}

	public void removeSession(String name) {
		this.sessionConnectionMonitor.removeSession(name);
	}

	public void setSshTunnelComposite(SshTunnelComposite sshTunnelComposite) {
		this.sessionConnectionMonitor.setSshTunnelComposite(sshTunnelComposite);
	}

	public static SessionConnectionMonitorFactory getInstance() {
		return INSTANCE;
	}
}
