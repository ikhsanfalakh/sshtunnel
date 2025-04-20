/*
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
import org.eclipse.swt.custom.TableEditor
import org.eclipse.swt.events.*
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.ConnectionManager
import org.programmerplanet.sshtunnel.model.Session

/**
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class SessionsComposite(
    parent: Composite,
    style: Int,
    private val sessions: MutableList<Session>,
    private val listener: SessionChangeListener
) :
    Composite(parent, style) {
    private var sessionTable: Table
    private var addSessionButton: Button
    private var editSessionButton: Button
    private var removeSessionButton: Button

    init {
        layout = FillLayout()
        val group = Group(this, SWT.NULL).apply {
            text = "Sessions"
            layout = GridLayout()
        }

        val buttonBarComposite = Composite(group, SWT.NONE).apply {
            layout = FillLayout()
            layoutData = GridData().apply { horizontalAlignment = GridData.END }
        }
        addSessionButton = Button(buttonBarComposite, SWT.PUSH)
        editSessionButton = Button(buttonBarComposite, SWT.PUSH).apply { isEnabled = false }
        removeSessionButton = Button(buttonBarComposite, SWT.PUSH).apply { isEnabled = false }

        val buttons: List<Triple<Button, String, ImageResource>> = listOf(
            Triple(addSessionButton, "Add", ImageResource.Add),
            Triple(editSessionButton, "Edit", ImageResource.Edit),
            Triple(removeSessionButton, "Remove", ImageResource.Remove)
        )

        buttons.forEach { (button, text, image) ->
            button.apply {
                this.text = text
                toolTipText = "$text Session"
                this.image = image.getImage(display)
            }
        }

        addSessionButton.onSelect { addSession() }
        editSessionButton.onSelect { editSession() }
        removeSessionButton.onSelect { removeSession() }

        sessionTable = Table(group, SWT.SINGLE or SWT.BORDER or SWT.FULL_SELECTION)
        createTable()
        updateTable()
    }

    private fun Button.onSelect(action: () -> Unit) {
        addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) = action()
        })
    }

    private fun createTable() {
        sessionTable.headerVisible = true
        sessionTable.linesVisible = true

        TableColumn(sessionTable, SWT.NULL).apply { text = " " }
        TableColumn(sessionTable, SWT.NULL).apply { text = "Name" }
        TableColumn(sessionTable, SWT.NULL).apply { text = "Hostname" }
        TableColumn(sessionTable, SWT.NULL).apply { text = "Port" }
        TableColumn(sessionTable, SWT.NULL).apply { text = "Username" }
        TableColumn(sessionTable, SWT.NULL).apply { text = "Autorun" }

        sessionTable.layoutData = GridData().apply {
            grabExcessHorizontalSpace = true
            horizontalAlignment = GridData.FILL
            grabExcessVerticalSpace = true
            verticalAlignment = GridData.FILL
        }

        sessionTable.addControlListener(object : ControlAdapter() {
            override fun controlResized(e: ControlEvent) {
                val iconColumnWidth = 25
                val portColumnWidth = 80
                val autorunColumnWidth = 55

                val area = sessionTable.clientArea
                var relativeWidth = area.width
                relativeWidth -= iconColumnWidth
                relativeWidth -= portColumnWidth
                relativeWidth -= autorunColumnWidth
                relativeWidth /= 3

                sessionTable.getColumn(0).width = iconColumnWidth
                sessionTable.getColumn(1).width = relativeWidth
                sessionTable.getColumn(2).width = relativeWidth
                sessionTable.getColumn(3).width = portColumnWidth
                sessionTable.getColumn(4).width = relativeWidth
                sessionTable.getColumn(5).width = autorunColumnWidth
            }
        })

        sessionTable.addMouseListener(object : MouseAdapter() {
            override fun mouseDoubleClick(e: MouseEvent) {
                editSession()
            }
        })

        sessionTable.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: SelectionEvent) {
                val row = sessionTable.selectionIndex
                editSessionButton.isEnabled = row > -1
                removeSessionButton.isEnabled = row > -1
                if (row > -1) {
                    val session: Session = sessions[row]
                    listener.sessionSelectionChanged(session)
                }
            }
        })
    }

    fun updateTable() {
        sessionTable.removeAll()
        sessions.sort()

        for (session in sessions) {
            val tableItem = TableItem(sessionTable, SWT.NULL)
            tableItem.setText(
                arrayOf(
                    "",
                    session.sessionName,
                    session.hostname,
                    session.port.toString(),
                    session.username,
                    ""
                )
            )
            val imageConnection: Image =
                if (ConnectionManager.isConnected(session)) ImageResource.Connected.getImage(display)
                else ImageResource.Disconnected.getImage(display)
            tableItem.setImage(0, imageConnection)

            val imageAutoRunning: Image? =
                if (session.isAutorunning) ImageResource.RunAuto.getImage(display) else null
            tableItem.setImage(5, imageAutoRunning)
        }
    }

    private fun addSession() {
        val session = Session("")
        val dialog = EditSessionDialog(this.shell, session)
        val result = dialog.open()
        if (result == SWT.OK) {
            sessions.add(session)
            updateTable()
            listener.sessionAdded(session)
        }
    }

    private fun editSession() {
        val row = sessionTable.selectionIndex
        if (row > -1) {
            val session = sessions[row]
            val dialog = EditSessionDialog(this.shell, session)
            val result = dialog.open()
            if (result == SWT.OK) {
                updateTable()
                listener.sessionChanged(session)
            }
        }
    }

    private fun removeSession() {
        val row = sessionTable.selectionIndex
        if (row > -1) {
            val session = sessions.removeAt(row)
            updateTable()
            listener.sessionRemoved(session)
        }
    }

}
