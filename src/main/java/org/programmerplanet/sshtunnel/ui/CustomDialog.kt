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
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.*

/**
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
abstract class CustomDialog(parent: Shell) : Dialog(parent, SWT.NONE) {
    private var result = SWT.CANCEL
    private var shell: Shell = Shell(parent, SWT.DIALOG_TRIM or SWT.APPLICATION_MODAL or SWT.RESIZE)

    fun open(): Int {
        shell.text = text
        shell.image = parent.image
        shell.layout = GridLayout()
        createContentComposite(shell)
        createButtonBarComposite(shell)
        shell.pack()
        centerShell(parent, shell)
        shell.open()
        val display: Display = parent.display
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        return result
    }

    private fun centerShell(parent: Shell, shell: Shell) {
        val parentBounds = parent.bounds
        val childBounds = shell.bounds
        val x = parentBounds.x + (parentBounds.width - childBounds.width) / 2
        val y = parentBounds.y + (parentBounds.height - childBounds.height) / 2
        shell.setLocation(x, y)
    }

    private fun createContentComposite(parent: Composite) {
        val contentComposite = Composite(parent, SWT.NONE)

        val gridData = GridData()
        gridData.grabExcessHorizontalSpace = true
        gridData.horizontalAlignment = GridData.FILL
        gridData.grabExcessVerticalSpace = true
        gridData.verticalAlignment = GridData.FILL
        contentComposite.layoutData = gridData

        initialize(contentComposite)
    }

    private fun createButtonBarComposite(parent: Composite) {
        val buttonBarComposite = Composite(parent, SWT.NONE)

        val gridData = GridData()
        gridData.grabExcessHorizontalSpace = true
        gridData.horizontalAlignment = GridData.CENTER
        buttonBarComposite.layoutData = gridData

        val rowLayout = RowLayout()
        rowLayout.pack = false
        buttonBarComposite.layout = rowLayout

        val okButton = Button(buttonBarComposite, SWT.PUSH)
        okButton.text = "OK"
        okButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                okPressed()
            }
        })

        shell.defaultButton = okButton

        val cancelButton = Button(buttonBarComposite, SWT.PUSH)
        cancelButton.text = "Cancel"
        cancelButton.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                cancelPressed()
            }
        })
    }

    protected open fun okPressed() {
        result = SWT.OK
        shell.close()
    }

    protected fun cancelPressed() {
        result = SWT.CANCEL
        shell.close()
    }

    protected abstract fun initialize(parent: Composite)
}