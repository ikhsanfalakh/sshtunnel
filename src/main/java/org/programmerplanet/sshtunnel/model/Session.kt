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
package org.programmerplanet.sshtunnel.model

/**
 * Represents a session to an ssh host.
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 * @author [Mulya Agung](agungm@outlook.com)
 */
class Session : Comparable<Session> {
    @JvmField
	var sessionName: String? = null
    @JvmField
	var hostname: String? = null
    @JvmField
	var port: Int = DEFAULT_PORT
    @JvmField
	var username: String? = null
    @JvmField
	var password: String? = null
    @JvmField
	val tunnels: MutableList<Tunnel> = ArrayList()
    @JvmField
	var identityPath: String? = null
    @JvmField
	var passPhrase: String? = null
    var isCompressed: Boolean = false
    @JvmField
	var ciphers: String? = null
    @JvmField
	var debugLogPath: String? = null

    override fun toString(): String {
        return "Session (" + sessionName + ": " + username + "@" + hostname + (if (port != DEFAULT_PORT) ":$port" else "") + ")"
    }

    override fun compareTo(other: Session): Int {
        return sessionName!!.compareTo(other.sessionName!!)
    }

    companion object {
        private const val DEFAULT_PORT = 22
    }
}
