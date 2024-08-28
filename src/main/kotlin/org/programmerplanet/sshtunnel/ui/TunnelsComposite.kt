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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.agung.sshtunnel.addon.CsvConfigImporter
import org.eclipse.swt.SWT
import org.eclipse.swt.events.*
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.Session
import org.programmerplanet.sshtunnel.model.Tunnel
import java.io.IOException
import java.util.*

private val TUNNEL_CONF_NAMES = arrayOf("CSV files")
private val TUNNEL_CONF_EXT = arrayOf("*.csv")

private val logger = KotlinLogging.logger {}

/**
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 * @author [Mulya Agung](agungm@outlook.com)
 */
class TunnelsComposite(
    parent: Composite,
    private val shell: Shell,
    style: Int,
    private val listener: TunnelChangeListener
) :
    Composite(parent, style) {
    private var tunnelTable: Table
    private var addTunnelButton: Button
    private var editTunnelButton: Button
    private var removeTunnelButton: Button
    private var importTunnelButton: Button
    private var exportTunnelButton: Button
    private var session: Session? = null

    private val csvConfigImporter = CsvConfigImporter()

    init {
        layout = FillLayout()
        val group = Group(this, SWT.NULL)
        group.text = "Tunnels"
        group.layout = GridLayout()

        val buttonBarComposite = Composite(group, SWT.NONE)
        addTunnelButton = Button(buttonBarComposite, SWT.PUSH)
        editTunnelButton = Button(buttonBarComposite, SWT.PUSH)
        removeTunnelButton = Button(buttonBarComposite, SWT.PUSH)
        importTunnelButton = Button(buttonBarComposite, SWT.PUSH)
        exportTunnelButton = Button(buttonBarComposite, SWT.PUSH)
        createButtonBarComposite(group, buttonBarComposite)
        tunnelTable = Table(group, SWT.SINGLE or SWT.BORDER or SWT.FULL_SELECTION)
        createTable()
        updateTable()
    }

    fun setSession(session: Session) {
        this.session = session
        addTunnelButton.isEnabled = true
        importTunnelButton.isEnabled = true
        exportTunnelButton.isEnabled = true
        updateTable()
    }

    private fun createButtonBarComposite(group: Group, buttonBarComposite: Composite) {
        buttonBarComposite.apply {
            layout = FillLayout()
            layoutData = GridData().apply {
                horizontalAlignment = GridData.END
            }
        }

        val buttons: List<Triple<Button, String, ImageResource>> = listOf(
            Triple(addTunnelButton, "Add", ImageResource.Add),
            Triple(editTunnelButton, "Edit", ImageResource.Edit),
            Triple(removeTunnelButton, "Remove", ImageResource.Remove),
            Triple(importTunnelButton, "Import", ImageResource.Import),
            Triple(exportTunnelButton, "Export", ImageResource.Export)
        )

        buttons.forEach { (button, text, image) ->
            button.apply {
                this.text = text
                toolTipText = "$text Tunnel"
                this.image = image.getImage(display)
                isEnabled = false
            }
        }

        addTunnelButton.onSelect { addTunnel() }
        editTunnelButton.onSelect { editTunnel() }
        removeTunnelButton.onSelect { removeTunnel() }

        importTunnelButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: SelectionEvent) {
                val dlg = FileDialog(group.shell, SWT.OPEN)
                dlg.text = "Import Tunnel Configuration"
                dlg.filterNames = TUNNEL_CONF_NAMES
                dlg.filterExtensions = TUNNEL_CONF_EXT
                val fn = dlg.open()
                if (fn != null) {
                    try {
                        importTunnels(fn)
                    } catch (e: IOException) {
                        logger.error(e) { "failed to import tunnels"}
                    }
                }
            }
        })

        exportTunnelButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: SelectionEvent) {
                val dlg = FileDialog(group.shell, SWT.SAVE)
                dlg.text = "Export Tunnel Configuration"
                dlg.filterNames = TUNNEL_CONF_NAMES
                dlg.filterExtensions = TUNNEL_CONF_EXT
                val fn = dlg.open()
                if (fn != null) {
                    exportTunnels(fn)
                }
            }
        })
    }

    private fun Button.onSelect(action: () -> Unit) {
        addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) = action()
        })
    }

    private fun createTable() {
        tunnelTable.headerVisible = true
        tunnelTable.linesVisible = true

        val column1 = TableColumn(tunnelTable, SWT.NULL)
        column1.text = "Local Address"

        val column2 = TableColumn(tunnelTable, SWT.NULL)
        column2.text = "Local Port"

        val column3 = TableColumn(tunnelTable, SWT.CENTER)
        column3.text = " "

        val column4 = TableColumn(tunnelTable, SWT.NULL)
        column4.text = "Remote Address"

        val column5 = TableColumn(tunnelTable, SWT.NULL)
        column5.text = "Remote Port"

        tunnelTable.layoutData = GridData().apply {
            grabExcessHorizontalSpace = true
            horizontalAlignment = GridData.FILL
            grabExcessVerticalSpace = true
            verticalAlignment = GridData.FILL
        }

        tunnelTable.addControlListener(object : ControlAdapter() {
            override fun controlResized(e: ControlEvent) {
                val directionColumnWidth = 30
                val portColumnWidth = 100
                val area = tunnelTable.clientArea
                val relativeWidth = (area.width - directionColumnWidth - (portColumnWidth * 2)) / 2
                tunnelTable.apply {
                    arrayOf(
                        relativeWidth,
                        portColumnWidth,
                        directionColumnWidth,
                        relativeWidth,
                        portColumnWidth
                    ).forEachIndexed { i, w ->
                        getColumn(i).width = w
                    }
                }
            }
        })

        tunnelTable.addMouseListener(object : MouseAdapter() {
            override fun mouseDoubleClick(e: MouseEvent) {
                editTunnel()
            }
        })

        tunnelTable.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: SelectionEvent) {
                val row = tunnelTable.selectionIndex
                editTunnelButton.isEnabled = row > -1
                removeTunnelButton.isEnabled = row > -1
                if (row > -1) {
                    val tunnel = session!!.tunnels[row]
                    listener.tunnelSelectionChanged(tunnel)
                }
            }
        })
    }

    fun updateTable() {
        tunnelTable.removeAll()
        if (session != null) {
            val red = display.getSystemColor(SWT.COLOR_RED)
            val tunnels: MutableList<Tunnel> = session!!.tunnels
            tunnels.sort()
            for (tunnel in tunnels) {
                val tableItem = TableItem(tunnelTable, SWT.NULL)
                tableItem.setText(
                    arrayOf(
                        tunnel.localAddress,
                        tunnel.localPort.toString(),
                        if (tunnel.local) "⇒" else "⇐",
                        tunnel.remoteAddress,
                        tunnel.remotePort.toString()
                    )
                )
                if (tunnel.exception != null) {
                    tableItem.foreground = red
                }
            }
        }
    }

    private fun addTunnel() {
        val tunnel = Tunnel()
        val dialog = EditTunnelDialog(getShell(), tunnel)
        val result = dialog.open()
        if (result == SWT.OK) {
            session!!.tunnels.add(tunnel)
            updateTable()
            listener.tunnelAdded(session!!, tunnel)
        }
    }

    private fun editTunnel() {
        val row = tunnelTable.selectionIndex
        if (row > -1) {
            val tunnel = session!!.tunnels[row]
            val prevTunnel = tunnel.copy()
            val dialog = EditTunnelDialog(getShell(), tunnel)
            val result = dialog.open()
            if (result == SWT.OK) {
                updateTable()
                val status = listener.tunnelChanged(session!!, tunnel, prevTunnel)
                if (status != 0) {
                    logger.error {"Unable to stop existing tunnel"}
                }
            }
        }
    }

    private fun removeTunnel() {
        val row = tunnelTable.selectionIndex
        if (row > -1) {
            val tunnel = session!!.tunnels.removeAt(row)
            updateTable()
            listener.tunnelRemoved(session!!, tunnel)
        }
    }

    @Throws(IOException::class)
    private fun importTunnels(csvPath: String) {
        val importedTunnels: MutableSet<Tunnel> = csvConfigImporter.readCsv(csvPath)
        if (importedTunnels.isEmpty()) {
            val messageBox = MessageBox(shell, SWT.ICON_INFORMATION or SWT.OK)
            messageBox.text = "Info"
            messageBox.message = "Imported file has no tunnel records"
            messageBox.open()
        } else {
            val messageBox = MessageBox(shell, SWT.ICON_WARNING or SWT.YES or SWT.NO)
            messageBox.text = "Warning"
            messageBox.message = """Found ${importedTunnels.size} records
Do you want to import them now?
(Existing tunnel will be merged)"""
            val result: Int = messageBox.open()
            val proceed: Boolean = (result == SWT.YES)

            if (proceed) {
                importedTunnels.addAll(session!!.tunnels)
                val it: MutableIterator<Tunnel> = session!!.tunnels.iterator()
                while (it.hasNext()) {
                    listener.tunnelRemoved(session!!, it.next())
                    it.remove()
                }
                for (tunnel: Tunnel in importedTunnels) {
                    session!!.tunnels.add(tunnel)
                    listener.tunnelAdded(session!!, tunnel)
                }
                updateTable()
            }
        }
    }

    private fun exportTunnels(csvPath: String) {
        try {
            csvConfigImporter.writeCsv(session!!.tunnels, csvPath)
        } catch (e: Exception) {
            val messageBox = MessageBox(shell, SWT.ICON_ERROR or SWT.OK)
            messageBox.text = "Error"
            messageBox.message = e.message
            messageBox.open()
        }
    }

}
