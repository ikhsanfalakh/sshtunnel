/*
 * Copyright 2023 Mulya Agung
 * Copyright 2009 Joseph Fifield
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
package org.programmerplanet.sshtunnel.ui

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.events.*
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.*
import org.programmerplanet.sshtunnel.model.ConnectionManager
import java.io.IOException
import java.io.InputStream
import kotlin.system.exitProcess

enum class ImageResource(private val fileName: String) {
    App("/images/sshtunnel.png"),
    Connect("/images/connect.png"),
    Disconnect("/images/disconnect.png"),
    ConnectAll("/images/connect_all.png"),
    DisconnectAll("/images/disconnect_all.png"),
    Connected("/images/bullet_green.png"),
    Disconnected("/images/bullet_red.png"),
    Add("/images/add.png"),
    Edit("/images/edit.png"),
    Remove("/images/delete.png"),
    Import("/images/import.png"),
    Export("/images/export.png");

    private var image: Image? = null

    fun getImage(display: Display): Image {
        if (image == null || image!!.isDisposed) {
            image = Image(display, this.javaClass.getResourceAsStream(fileName))
        }
        return image!!
    }

    companion object {
        fun disposeAll() {
            entries.forEach { it.image?.dispose() }
        }
    }
}

/**
 * @author [Mulya Agung](agungm@outlook.com)
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class ApplicationComposite(private val shell: Shell) : Composite(shell, SWT.NONE) {

    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button

    private lateinit var connectAllButton: Button
    private lateinit var disconnectAllButton: Button
    private lateinit var trayItem: TrayItem

    private lateinit var progressBar: ProgressBar

    private var configuration: Configuration = Configuration()
    private var sashForm: SashForm
    private var sessionsComposite: SessionsComposite
    private var tunnelsComposite: TunnelsComposite? = null

    private var currentSession: Session? = null

    init {
        try {
            configuration.read()
        } catch (e: IOException) {
            val messageBox = MessageBox(shell, SWT.ICON_ERROR or SWT.OK)
            messageBox.text = "Error"
            messageBox.message = "Unable to load configuration."
            messageBox.open()
        }
        shell.text = APPLICATION_TITLE
        shell.layout = FillLayout()
        shell.image = ImageResource.App.getImage(display)
        shell.addShellListener(object : ShellAdapter() {
            override fun shellClosed(e: ShellEvent) {
                exit()
            }

            override fun shellIconified(e: ShellEvent) {
                shell.minimized = true
            }
        })
        layout = GridLayout()
        sashForm = SashForm(this, SWT.VERTICAL)
        createSashForm()
        val tunnelChangeListener: TunnelChangeListener = object : TunnelChangeListener {
            override fun tunnelAdded(session: Session, tunnel: Tunnel) {
                ConnectionManager.startTunnelIfSessionConnected(session, tunnel)
            }

            override fun tunnelChanged(session: Session, tunnel: Tunnel, prevTunnel: Tunnel?): Int {
                return ConnectionManager.changeTunnelIfSessionConnected(session, tunnel, prevTunnel!!)
            }

            override fun tunnelRemoved(session: Session, tunnel: Tunnel) {
                ConnectionManager.stopTunnelIfSessionConnected(session, tunnel)
            }

            override fun tunnelSelectionChanged(tunnel: Tunnel) {
            }

        }
        val sessionChangeListener: SessionChangeListener = object : SessionChangeListener {
            override fun sessionAdded(session: Session) {
                updateConnectButtons()
            }

            override fun sessionChanged(session: Session) {
            }

            override fun sessionRemoved(session: Session) {
                updateConnectButtons()
            }

            override fun sessionSelectionChanged(session: Session) {
                currentSession = session
                tunnelsComposite?.setSession(session)
                updateConnectButtons()
            }
        }
        sessionsComposite = SessionsComposite(sashForm, SWT.NONE, configuration.sessions, sessionChangeListener)
        tunnelsComposite = TunnelsComposite(sashForm, shell, SWT.NONE, tunnelChangeListener)

        createButtonBarComposite()
        createStatusBarComposite()
        createTrayIcon()
        shell.setBounds(configuration.left, configuration.top, configuration.width, configuration.height)
        sashForm.weights = configuration.weights
        // Run connection monitor
        SessionConnectionMonitorFactory.setSshTunnelComposite(this)
        SessionConnectionMonitorFactory.startMonitor()
    }

    private fun save() {
        configuration.left = shell.bounds.x
        configuration.top = shell.bounds.y
        configuration.width = shell.bounds.width
        configuration.height = shell.bounds.height
        configuration.weights = sashForm.weights
        try {
            configuration.write()
        } catch (e: IOException) {
            val messageBox = MessageBox(shell, SWT.ICON_ERROR or SWT.OK)
            messageBox.text = "Error"
            messageBox.message = "Unable to save configuration."
            messageBox.open()
        }
    }

    private fun loadImage(path: String): Image {
        val stream: InputStream? = ApplicationComposite::class.java.getResourceAsStream(path)
        try {
            return Image(this.display, stream)
        } finally {
            try {
                stream?.close()
            } catch (e: IOException) {
                // ignore
            }
        }
    }

    private fun createSashForm() {
        val gridData = GridData()
        gridData.grabExcessHorizontalSpace = true
        gridData.horizontalAlignment = GridData.FILL
        gridData.grabExcessVerticalSpace = true
        gridData.verticalAlignment = GridData.FILL
        sashForm.layoutData = gridData
    }

    private fun createButtonBarComposite() {
        val buttonBarComposite = Composite(this, SWT.NONE)
        buttonBarComposite.layout = FillLayout()

        val gridData = GridData()
        gridData.horizontalAlignment = GridData.CENTER
        buttonBarComposite.layoutData = gridData

        connectButton = Button(buttonBarComposite, SWT.PUSH)
        connectButton.text = "Connect"
        connectButton.toolTipText = "Connect"
        connectButton.image = ImageResource.Connect.getImage(display)
        connectButton.isEnabled = false

        disconnectButton = Button(buttonBarComposite, SWT.PUSH)
        disconnectButton.text = "Disconnect"
        disconnectButton.toolTipText = "Disconnect"
        disconnectButton.image = ImageResource.Disconnect.getImage(display)
        disconnectButton.isEnabled = false

        connectAllButton = Button(buttonBarComposite, SWT.PUSH)
        connectAllButton.text = "Connect All"
        connectAllButton.toolTipText = "Connect All"
        connectAllButton.image = ImageResource.ConnectAll.getImage(display)
        connectAllButton.isEnabled = true

        disconnectAllButton = Button(buttonBarComposite, SWT.PUSH)
        disconnectAllButton.text = "Disconnect All"
        disconnectAllButton.toolTipText = "Disconnect All"
        disconnectAllButton.image = ImageResource.DisconnectAll.getImage(display)
        disconnectAllButton.isEnabled = false

        connectButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                val display = this@ApplicationComposite.display
                val runnable = Runnable { connect() }
                display.asyncExec(runnable)
            }
        })

        disconnectButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                val display = this@ApplicationComposite.display
                val runnable = Runnable { disconnect() }
                display.asyncExec(runnable)
            }
        })

        connectAllButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                val display = this@ApplicationComposite.display
                val runnable = Runnable { connectAll() }
                display.asyncExec(runnable)
            }
        })

        disconnectAllButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                val display = this@ApplicationComposite.display
                val runnable = Runnable { disconnectAll() }
                display.asyncExec(runnable)
            }
        })
    }

    private fun createStatusBarComposite() {
        val statusBarComposite = Composite(this, SWT.NONE)

        val statusBarGridLayout = GridLayout(3, false)
        statusBarGridLayout.marginRight = 2
        statusBarGridLayout.marginTop = 0
        statusBarComposite.layout = statusBarGridLayout
        statusBarComposite.layoutData = GridData(SWT.FILL, SWT.END, true, false)

        val siteLabel = Label(statusBarComposite, SWT.NONE)
        siteLabel.text = APPLICATION_SITE
        siteLabel.isEnabled = false
        siteLabel.layoutData = GridData(SWT.BEGINNING, SWT.CENTER, true, false)

        progressBar = ProgressBar(statusBarComposite, SWT.INDETERMINATE)
        progressBar.layoutData = GridData(SWT.END, SWT.CENTER, true, false)
        progressBar.isVisible = false

        val versionLabel = Label(statusBarComposite, SWT.NONE)
        versionLabel.text = "   $APPLICATION_VERSION"
        versionLabel.isEnabled = false
        versionLabel.layoutData = GridData(SWT.END, SWT.CENTER, false, false)
    }

    private fun showProgressBar() {
        connectButton.isEnabled = false
        connectAllButton.isEnabled = false
        disconnectAllButton.isEnabled = false
        progressBar.isVisible = true
    }

    private fun stopProgressBar() {
        progressBar.isVisible = false
    }

    fun connectionStatusChanged() {
        sessionsComposite.updateTable()
        tunnelsComposite?.updateTable()
        updateConnectButtons()
        stopProgressBar()
    }

    private fun updateConnectButtons() {
        connectButton.isEnabled = !ConnectionManager.isConnected(currentSession)
        disconnectButton.isEnabled = ConnectionManager.isConnected(currentSession)
        connectAllButton.isEnabled = anyDisconnectedSessions()
        disconnectAllButton.isEnabled = anyConnectedSessions()
    }

    private fun anyDisconnectedSessions(): Boolean {
        var result = false
        for (session in configuration.sessions) {
            if (!ConnectionManager.isConnected(session)) {
                result = true
                break
            }
        }
        return result
    }

    private fun anyConnectedSessions(): Boolean {
        var result = false
        for (session in configuration.sessions) {
            if (ConnectionManager.isConnected(session)) {
                result = true
                break
            }
        }
        return result
    }

    private fun connect(session: Session? = currentSession) {
        save()
        if (session != null && !ConnectionManager.isConnected(session)) {
            showProgressBar()

            Thread {
                try {
                    ConnectionManager.connect(session, shell)
                    // Put to monitored list
                    SessionConnectionMonitorFactory.addSession(session.sessionName, session)
                } catch (ce: ConnectionException) {
                    try {
                        ConnectionManager.disconnect(session)
                    } catch (ignored: Exception) {
                    }
                    Display.getDefault().asyncExec {
                        showErrorMessage(
                            "Unable to connect to '" + session.sessionName + "'",
                            ce
                        )
                    }
                }
                Display.getDefault().asyncExec { this.connectionStatusChanged() }
            }.start()
        }
    }

    private fun showErrorMessage(message: String, e: Exception) {
        val cause = getOriginatingCause(e)
        val messageBox = MessageBox(shell, SWT.ICON_ERROR or SWT.OK)
        messageBox.text = "Error"
        messageBox.message = message + ": " + cause.message
        messageBox.open()
    }

    fun showDisconnectedMessage(session: Session) {
        if (trayItem.visible) {
            val tip = ToolTip(shell, SWT.BALLOON or SWT.ICON_ERROR)
            tip.text = "Session: " + session.sessionName
            tip.message = "Connection to " + session.hostname + " has been lost."
            trayItem.toolTip = tip
            tip.isVisible = true
        }
    }

    private fun getOriginatingCause(e: Exception): Throwable {
        var cause: Throwable = e
        while (cause.cause != null) {
            cause = cause.cause!!
        }
        return cause
    }

    private fun disconnect(session: Session? = currentSession) {
        save()
        if (session != null && ConnectionManager.isConnected(session)) {
            ConnectionManager.disconnect(session)
            SessionConnectionMonitorFactory.removeSession(session.sessionName)
        }
        connectionStatusChanged()
    }

    private fun connectAll() {
        save()
        showProgressBar()

        Thread {
            var sessionToConnect: Session? = null
            try {
                for (session: Session in configuration.sessions) {
                    sessionToConnect = session
                    if (!ConnectionManager.isConnected(sessionToConnect)) {
                        ConnectionManager.connect(sessionToConnect, shell)
                    }
                }
            } catch (ce: ConnectionException) {
                for (session: Session in configuration.sessions) {
                    try {
                        ConnectionManager.disconnect(session)
                    } catch (ignored: Exception) {
                    }
                }

                val failedSession: Session? = sessionToConnect
                Display.getDefault().asyncExec {
                    showErrorMessage(
                        "Unable to connect to '" + failedSession!!.sessionName + "'",
                        ce
                    )
                }
            }
            Display.getDefault().asyncExec { this.connectionStatusChanged() }
        }.start()
    }

    private fun disconnectAll() {
        save()
        for (session in configuration.sessions) {
            if (ConnectionManager.isConnected(session)) {
                ConnectionManager.disconnect(session)
            }
        }
        connectionStatusChanged()
    }

    private fun createTrayIcon() {
        val tray: Tray = display.systemTray

        trayItem = TrayItem(tray, 0)
        trayItem.toolTipText = APPLICATION_TITLE
        trayItem.image = ImageResource.App.getImage(display)

        trayItem.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                shell.minimized = !shell.minimized
            }
        })

        trayItem.addMenuDetectListener { e: MenuDetectEvent ->
            val s = Shell(e.display)
            val m = Menu(s, SWT.POP_UP)

            // build menu
            val menuItemListener =
                Listener { event: Event ->
                    val source = event.widget.data
                    if (source is Session) {
                        val runnable = Runnable {
                            val connectedBefore = ConnectionManager.isConnected(source)
                            if (connectedBefore) {
                                disconnect(source)
                            } else {
                                connect(source)
                            }
                            val connectedAfter = ConnectionManager.isConnected(source)
                            if (connectedBefore != connectedAfter) {
                                val title = "Session: " + source.sessionName
                                val message =
                                    source.sessionName + " is now " + (if (connectedAfter) "connected" else "disconnected") + "."
                                val tip = ToolTip(shell, SWT.BALLOON or SWT.ICON_INFORMATION)
                                tip.text = title
                                tip.message = message
                                trayItem.toolTip = tip
                                tip.isVisible = true
                            }
                        }
                        event.display.asyncExec(runnable)
                    }
                }

            for (session in configuration.sessions) {
                val menuItem = MenuItem(m, SWT.NONE)
                menuItem.data = session
                val text = session.sessionName
                menuItem.text = text
                val image =
                    if (ConnectionManager.isConnected(session)) ImageResource.Connect.getImage(display) else ImageResource.Disconnect.getImage(
                        display
                    )
                menuItem.image = image
                menuItem.addListener(SWT.Selection, menuItemListener)
            }

            if (configuration.sessions.isNotEmpty()) {
                MenuItem(m, SWT.SEPARATOR)
            }

            val exit = MenuItem(m, SWT.NONE)
            exit.text = "Exit"
            exit.addListener(SWT.Selection) { exit() }
            m.isVisible = true
        }
    }

    private fun exit() {
        disconnectAll()
        SessionConnectionMonitorFactory.stopMonitor()
        save()
        val tray = display.systemTray
        for (trayItem in tray.items) {
            trayItem.dispose()
        }
        exitProcess(0)
    }

    /**
     * @see org.eclipse.swt.widgets.Widget.dispose
     */
    override fun dispose() {
        ImageResource.disposeAll()
        super.dispose()
    }

    companion object {
        const val APPLICATION_TITLE: String = "SSH Tunnel NG"
        private const val APPLICATION_VERSION = "v0.7"
        private const val APPLICATION_SITE = "github.com/agung-m/sshtunnel-ng"
    }
}

