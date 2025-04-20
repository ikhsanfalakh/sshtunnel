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
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import org.programmerplanet.sshtunnel.model.Tunnel

/**
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class EditTunnelDialog(parent: Shell, private val tunnel: Tunnel) : CustomDialog(parent) {

    private lateinit var localAddressText: Text
    private lateinit var localPortText: Text
    private lateinit var remoteAddressText: Text
    private lateinit var remotePortText: Text
    private lateinit var localRadioButton: Button
    private lateinit var remoteRadioButton: Button

    init {
        this.text = "Tunnel"
    }

    override fun initialize(parent: Composite) {
        val layout = GridLayout()
        layout.numColumns = 2
        parent.layout = layout
        val defaultFont = parent.font

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Local Address:"
            font = defaultFont
        }

        localAddressText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Local Port:"
            font = defaultFont
        }

        localPortText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Remote Address:"
            font = defaultFont
        }

        remoteAddressText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Remote Port:"
            font = defaultFont
        }

        remotePortText = Text(parent, SWT.SINGLE or SWT.BORDER).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        Label(parent, SWT.RIGHT).apply {
            layoutData = GridData(GridData.END, GridData.CENTER, false, false)
            text = "Direction:"
            font = defaultFont
        }

        val directionComposite = Composite(parent, SWT.NONE).apply {
            layoutData = GridData(GridData.FILL, GridData.CENTER, true, false).apply {widthHint = 200}
        }

        directionComposite.layout = FillLayout()

        localRadioButton = Button(directionComposite, SWT.RADIO).apply { text = "Local" }
        remoteRadioButton = Button(directionComposite, SWT.RADIO).apply { text = "Remote" }

        localAddressText.text = tunnel.localAddress.orEmpty()
        localPortText.text = tunnel.localPort.takeIf { it > 0 }?.toString().orEmpty()
        remoteAddressText.text = tunnel.remoteAddress.orEmpty()
        remotePortText.text = tunnel.remotePort.takeIf { it > 0 }?.toString().orEmpty()
        localRadioButton.selection = tunnel.local
        remoteRadioButton.selection = !tunnel.local
    }

    override fun okPressed() {
        tunnel.apply {
            localAddress = localAddressText.text.orEmpty()
            localPort = localPortText.text.toIntOrNull() ?: 0
            remoteAddress = remoteAddressText.text.orEmpty()
            remotePort = remotePortText.text.toIntOrNull() ?: 0
            local = localRadioButton.selection
        }
        super.okPressed()
    }

}
