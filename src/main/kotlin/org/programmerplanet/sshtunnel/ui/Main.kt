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

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.programmerplanet.sshtunnel.util.AppInfo
import org.programmerplanet.sshtunnel.util.SingleInstanceChecker

/**
 * Application entry point.
 *
 * @author [Mulya Agung](agungm@outlook.com)
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (SingleInstanceChecker.isAppAlreadyRunning()) {
            println("Aplikasi sudah berjalan.")
            return
        }

        Display.setAppName(AppInfo.title)
        val display = Display()
        val shell = Shell(display)
        ApplicationComposite(shell)
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }
}
