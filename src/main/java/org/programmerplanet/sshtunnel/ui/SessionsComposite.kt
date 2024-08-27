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
import org.eclipse.swt.events.*
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.ConnectionManager.Companion.isConnected
import org.programmerplanet.sshtunnel.model.Session
import java.io.IOException

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
    private var editSessionButton: Button
    private var removeSessionButton: Button

    private var connectedImage: Image
    private var disconnectedImage: Image
    private var addImage: Image
    private var editImage: Image
    private var deleteImage: Image

    init {
        connectedImage = loadImage(CONNECTED_IMAGE_PATH)
        disconnectedImage = loadImage(DISCONNECTED_IMAGE_PATH)
        addImage = loadImage(ADD_IMAGE_PATH)
        editImage = loadImage(EDIT_IMAGE_PATH)
        deleteImage = loadImage(DELETE_IMAGE_PATH)
        layout = FillLayout()
        val group = Group(this, SWT.NULL)
        group.text = "Sessions"
        group.layout = GridLayout()

        val buttonBarComposite = Composite(group, SWT.NONE)
        buttonBarComposite.layout = FillLayout()

        val gridData = GridData()
        gridData.horizontalAlignment = GridData.END
        buttonBarComposite.layoutData = gridData

        val addSessionButton = Button(buttonBarComposite, SWT.PUSH)
        addSessionButton.text = "Add"
        addSessionButton.toolTipText = "Add Session"
        addSessionButton.image = addImage

        editSessionButton = Button(buttonBarComposite, SWT.PUSH)
        removeSessionButton = Button(buttonBarComposite, SWT.PUSH)
        createButtonBarComposite(addSessionButton)

        sessionTable = Table(group, SWT.SINGLE or SWT.BORDER or SWT.FULL_SELECTION)
        createTable()
        updateTable()
    }

    private fun createButtonBarComposite(addSessionButton: Button) {
        editSessionButton.text = "Edit"
        editSessionButton.toolTipText = "Edit Session"
        editSessionButton.image = editImage
        editSessionButton.isEnabled = false

        removeSessionButton.text = "Remove"
        removeSessionButton.toolTipText = "Remove Session"
        removeSessionButton.image = deleteImage
        removeSessionButton.isEnabled = false

        addSessionButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                addSession()
            }
        })

        editSessionButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                editSession()
            }
        })

        removeSessionButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                removeSession()
            }
        })
    }

    private fun createTable() {
        sessionTable.headerVisible = true
        sessionTable.linesVisible = true

        val column1 = TableColumn(sessionTable, SWT.NULL)
        column1.text = " "

        val column2 = TableColumn(sessionTable, SWT.NULL)
        column2.text = "Name"

        val column3 = TableColumn(sessionTable, SWT.NULL)
        column3.text = "Hostname"

        val column4 = TableColumn(sessionTable, SWT.NULL)
        column4.text = "Port"

        val column5 = TableColumn(sessionTable, SWT.NULL)
        column5.text = "Username"

        val gridData = GridData()
        gridData.grabExcessHorizontalSpace = true
        gridData.horizontalAlignment = GridData.FILL
        gridData.grabExcessVerticalSpace = true
        gridData.verticalAlignment = GridData.FILL
        sessionTable.layoutData = gridData

        sessionTable.addControlListener(object : ControlAdapter() {
            override fun controlResized(e: ControlEvent) {
                val iconColumnWidth = 22
                val portColumnWidth = 100

                val area = sessionTable.clientArea
                var relativeWidth = area.width
                relativeWidth -= iconColumnWidth
                relativeWidth -= portColumnWidth
                relativeWidth /= 3

                sessionTable.getColumn(0).width = iconColumnWidth
                sessionTable.getColumn(1).width = relativeWidth
                sessionTable.getColumn(2).width = relativeWidth
                sessionTable.getColumn(3).width = portColumnWidth
                sessionTable.getColumn(4).width = relativeWidth
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

    private fun loadImage(path: String): Image {
        try {
            SessionsComposite::class.java.getResourceAsStream(path).use { stream ->
                return Image(
                    this.display, stream
                )
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
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
                    session.username
                )
            )
            val image = if (isConnected(session)) connectedImage else disconnectedImage
            tableItem.setImage(0, image)
        }
    }

    private fun addSession() {
        val session = Session()
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

    private fun disposeImages() {
        connectedImage.dispose()
        disconnectedImage.dispose()
        addImage.dispose()
        editImage.dispose()
        deleteImage.dispose()
    }

    /**
     * @see org.eclipse.swt.widgets.Widget.dispose
     */
    override fun dispose() {
        disposeImages()
        super.dispose()
    }

    companion object {
        private const val CONNECTED_IMAGE_PATH = "/images/bullet_green.png"
        private const val DISCONNECTED_IMAGE_PATH = "/images/bullet_red.png"
        private const val ADD_IMAGE_PATH = "/images/add.png"
        private const val EDIT_IMAGE_PATH = "/images/edit.png"
        private const val DELETE_IMAGE_PATH = "/images/delete.png"
    }
}
