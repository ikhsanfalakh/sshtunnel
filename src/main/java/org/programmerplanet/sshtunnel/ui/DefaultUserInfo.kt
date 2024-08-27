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

import com.jcraft.jsch.UserInfo
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell

/**
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class DefaultUserInfo @JvmOverloads constructor(
    private val parent: Shell,
    private var password: String? = null
) :
    UserInfo {

    override fun promptPassword(message: String): Boolean {
        return true
    }

    override fun getPassword(): String? {
        return password
    }

    override fun promptPassphrase(message: String): Boolean {
        return true
    }

    override fun getPassphrase(): String? {
        return null
    }

    override fun promptYesNo(str: String): Boolean {
        val promptDialog: PromptRunnable = object : PromptRunnable() {
            override fun run() {
                val messageBox = MessageBox(parent, SWT.ICON_WARNING or SWT.YES or SWT.NO)
                messageBox.text = "Warning"
                messageBox.message = str
                val result = messageBox.open()
                returnState = (result == SWT.YES)
            }
        }
        Display.getDefault().syncExec(promptDialog)
        return promptDialog.returnState
    }

    override fun showMessage(message: String) {
        val promptDialog: PromptRunnable = object : PromptRunnable() {
            override fun run() {
                val messageBox = MessageBox(parent, SWT.ICON_INFORMATION or SWT.OK)
                messageBox.text = "Message"
                messageBox.message = message
                messageBox.open()
            }
        }
        Display.getDefault().syncExec(promptDialog)
    }

    internal open class PromptRunnable : Runnable {
        var returnState: Boolean = false

        override fun run() {
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
    }
}