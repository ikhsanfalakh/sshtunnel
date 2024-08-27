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

import org.agung.sshtunnel.addon.CsvConfigImporter
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.eclipse.swt.SWT
import org.eclipse.swt.events.*
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.Session
import org.programmerplanet.sshtunnel.model.Tunnel
import java.io.IOException
import java.util.*

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
    private var addImage: Image
    private var editImage: Image
    private var deleteImage: Image
    private var importImage: Image
    private var exportImage: Image

    private val csvConfigImporter = CsvConfigImporter()

    init {
        addImage = loadImage(ADD_IMAGE_PATH)
        editImage = loadImage(EDIT_IMAGE_PATH)
        deleteImage = loadImage(DELETE_IMAGE_PATH)
        importImage = loadImage(IMPORT_IMAGE_PATH)
        exportImage = loadImage(EXPORT_IMAGE_PATH)
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
        buttonBarComposite.layout = FillLayout()

        val gridData = GridData()
        gridData.horizontalAlignment = GridData.END
        buttonBarComposite.layoutData = gridData

        addTunnelButton.text = "Add"
        addTunnelButton.toolTipText = "Add Tunnel"
        addTunnelButton.image = addImage
        addTunnelButton.isEnabled = false

        editTunnelButton.text = "Edit"
        editTunnelButton.toolTipText = "Edit Tunnel"
        editTunnelButton.image = editImage
        editTunnelButton.isEnabled = false

        removeTunnelButton.text = "Remove"
        removeTunnelButton.toolTipText = "Remove Tunnel"
        removeTunnelButton.image = deleteImage
        removeTunnelButton.isEnabled = false

        importTunnelButton.text = "Import"
        importTunnelButton.toolTipText = "Import Tunnels from a CSV file"
        importTunnelButton.image = importImage
        importTunnelButton.isEnabled = false

        exportTunnelButton.text = "Export"
        exportTunnelButton.toolTipText = "Export Tunnels to a CSV file"
        exportTunnelButton.image = exportImage
        exportTunnelButton.isEnabled = false

        addTunnelButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                addTunnel()
            }
        })

        editTunnelButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                editTunnel()
            }
        })

        removeTunnelButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                removeTunnel()
            }
        })

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
                        log.error("failed to import tunnels", e)
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

        val gridData = GridData()
        gridData.grabExcessHorizontalSpace = true
        gridData.horizontalAlignment = GridData.FILL
        gridData.grabExcessVerticalSpace = true
        gridData.verticalAlignment = GridData.FILL
        tunnelTable.layoutData = gridData

        tunnelTable.addControlListener(object : ControlAdapter() {
            override fun controlResized(e: ControlEvent) {
                val directionColumnWidth = 30
                val portColumnWidth = 100

                val area = tunnelTable.clientArea
                var relativeWidth = area.width
                relativeWidth -= directionColumnWidth
                relativeWidth -= (portColumnWidth * 2)
                relativeWidth /= 2

                tunnelTable.getColumn(0).width = relativeWidth
                tunnelTable.getColumn(1).width = portColumnWidth
                tunnelTable.getColumn(2).width = directionColumnWidth
                tunnelTable.getColumn(3).width = relativeWidth
                tunnelTable.getColumn(4).width = portColumnWidth
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
                    // Failed cancelling tunnel bind
                    log.error("Unable to stop existing tunnel")
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

    private fun loadImage(path: String): Image {
        val stream = SessionsComposite::class.java.getResourceAsStream(path)
        try {
            return Image(this.display, stream)
        } finally {
            try {
                Objects.requireNonNull(stream).close()
            } catch (e: IOException) {
                // ignore
            }
        }
    }

    private fun disposeImages() {
        addImage.dispose()
        editImage.dispose()
        deleteImage.dispose()
        importImage.dispose()
        exportImage.dispose()
    }

    /**
     * @see org.eclipse.swt.widgets.Widget.dispose
     */
    override fun dispose() {
        disposeImages()
        super.dispose()
    }

    companion object {
        private val log: Log = LogFactory.getLog(TunnelsComposite::class.java)

        private const val ADD_IMAGE_PATH = "/images/add.png"
        private const val EDIT_IMAGE_PATH = "/images/edit.png"
        private const val DELETE_IMAGE_PATH = "/images/delete.png"
        private const val IMPORT_IMAGE_PATH = "/images/import.png"
        private const val EXPORT_IMAGE_PATH = "/images/export.png"
        private val TUNNEL_CONF_NAMES = arrayOf("CSV files")
        private val TUNNEL_CONF_EXT = arrayOf("*.csv")
    }
}
