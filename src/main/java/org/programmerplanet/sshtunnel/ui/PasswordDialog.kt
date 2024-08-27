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
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text

/**
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class PasswordDialog(parent: Shell) : CustomDialog(parent) {
    var message: String? = null
    private var passText: Text? = null
    var password: String? = null
        private set

    init {
        this.text = "Password"
    }

    override fun initialize(parent: Composite) {
        val layout = GridLayout()
        parent.layout = layout

        if (message != null) {
            val messageLabel = Label(parent, SWT.LEFT)
            val gridData = GridData(GridData.BEGINNING, GridData.CENTER, false, false)
            messageLabel.layoutData = gridData
            messageLabel.text = "$message:"
        }

        passText = Text(parent, SWT.SINGLE or SWT.BORDER or SWT.PASSWORD)
        val gridData4 = GridData(GridData.FILL, GridData.CENTER, true, false)
        gridData4.widthHint = 200
        passText!!.layoutData = gridData4
    }

    override fun okPressed() {
        password = passText!!.text
        super.okPressed()
    }
}
