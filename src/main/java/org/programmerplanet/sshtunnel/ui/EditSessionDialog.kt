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
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.Session

/**
 * @author [Mulya Agung](agungm@outlook.com)
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class EditSessionDialog(parent: Shell, private val session: Session) :
    CustomDialog(parent) {
    private lateinit var nameText: Text
    private lateinit var hostText: Text
    private lateinit var portText: Text
    private lateinit var userText: Text
    private lateinit var savePassCheckbox: Button
    private lateinit var passText: Text
    private lateinit var privKeyText: Text
    private lateinit var passPhraseText: Text
    private lateinit var compressionCheckbox: Button
    private lateinit var ciphersText: Text
    private lateinit var chooseCiphersCheckbox: Button
    private lateinit var debugLogDirText: Text

    init {
        this.text = "Session"
    }

    override fun initialize(parent: Composite) {
        val layout = GridLayout()
        layout.numColumns = 2
        parent.layout = layout

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Name:"
        }

        nameText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Host:"
        }

        hostText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Port:"
        }

        portText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        val userLabel = Label(parent, SWT.RIGHT)
        userLabel.layoutData = GridData(GridData.END, GridData.CENTER, false, false)
        userLabel.text = "Username:"

        userText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        savePassCheckbox = Button(parent, SWT.CHECK).apply {
            text = "Password"
            addSelectionListener(object : SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent) {
                    setSavePassword(savePassCheckbox.selection)
                }
            })
        }

        passText = Text(parent, SWT.SINGLE or SWT.BORDER or SWT.PASSWORD).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        val privKeyButton = Button(parent, SWT.PUSH)
        privKeyButton.text = "Identify file"
        privKeyButton.layoutData = GridData(GridData.END, GridData.CENTER, false, false)
        privKeyButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: SelectionEvent) {
                val dlg = FileDialog(parent.shell, SWT.OPEN)
                dlg.filterNames = PRIVATE_KEY_NAMES
                dlg.filterExtensions = PRIVATE_KEY_EXT
                val fn = dlg.open()
                if (fn != null) {
                    privKeyText.text = fn
                }
            }
        })

        privKeyText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        val passPhraseLabel = Label(parent, SWT.RIGHT)
        passPhraseLabel.layoutData = GridData(GridData.END, GridData.CENTER, false, false)
        passPhraseLabel.text = "Passphrase:"

        passPhraseText = Text(parent, SWT.SINGLE or SWT.BORDER or SWT.PASSWORD).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        chooseCiphersCheckbox = Button(parent, SWT.CHECK).apply {
            text = "Ciphers"
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            addSelectionListener(object : SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent) {
                    setChooseCiphers(selection)
                }
            })
        }

        ciphersText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        val debugLogDirButton = Button(parent, SWT.PUSH)
        debugLogDirButton.text = "Log directory"
        debugLogDirButton.layoutData = GridData(GridData.END, GridData.CENTER, false, false)
        debugLogDirButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: SelectionEvent) {
                val dlg = DirectoryDialog(parent.shell)
                val fn = dlg.open()
                if (fn != null) {
                    debugLogDirText.text = fn
                }
            }
        })

        debugLogDirText = Text(parent, SWT.SINGLE or SWT.BORDER)
        debugLogDirText.layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}

        Label(parent, SWT.LEAD).layoutData = GridData(GridData.END, GridData.END, false, false)

        compressionCheckbox = Button(parent, SWT.CHECK)
        compressionCheckbox.text = "Compression"

        nameText.text = session.sessionName
        hostText.text= session.hostname.orEmpty()
        portText.text = session.port.takeIf { it > 0 }?.toString().orEmpty()
        userText.text = session.username.orEmpty()

        if ((session.password != null) && (session.password?.trim { it <= ' ' }?.isNotEmpty() == true)) {
            setSavePassword(true)
        }
        else {
            setSavePassword(false)
        }

        privKeyText.text = session.identityPath.orEmpty()
        passPhraseText.text = session.passPhrase.orEmpty()

        if ((session.ciphers != null) && (session.ciphers?.trim { it <= ' ' }?.isNotEmpty() == true)) {
            setChooseCiphers(true)
        }
        else {
            setChooseCiphers(false)
        }
        debugLogDirText.text = session.debugLogPath.orEmpty()
        compressionCheckbox.selection = session.isCompressed
    }

    override fun okPressed() {
        session.apply {
            sessionName = nameText.text.orEmpty()
            hostname = hostText.text.orEmpty()
            port = portText.text.toIntOrNull() ?: 0
            username = userText.text.orEmpty()
            password = passText.text.orEmpty()
            identityPath = privKeyText.text.orEmpty()
            passPhrase = passPhraseText.text.orEmpty()
            isCompressed = compressionCheckbox.selection
            ciphers = ciphersText.text.orEmpty()
            debugLogPath = debugLogDirText.text.orEmpty()
        }
        super.okPressed()
    }

    private fun setSavePassword(savePassword: Boolean) {
        savePassCheckbox.selection = savePassword
        passText.isEnabled = savePassword
        passText.editable = savePassword
        if (!savePassword) {
            passText.text = ""
        }
    }

    private fun setChooseCiphers(isChosen: Boolean) {
        chooseCiphersCheckbox.selection = isChosen
        ciphersText.isEnabled = isChosen
        ciphersText.editable = isChosen
        if (!isChosen) {
            ciphersText.text = ""
        }
    }

    companion object {
        private val PRIVATE_KEY_NAMES = arrayOf("All Files")
        private val PRIVATE_KEY_EXT = arrayOf("*")
    }
}