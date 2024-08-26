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

import java.util.*

/**
 * Represents a tunnel (port forward) over an ssh connection.
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 * @author [Mulya Agung](agungm@outlook.com)
 */
class Tunnel : Comparable<Tunnel> {
    @JvmField
	var localAddress: String? = null
    @JvmField
	var localPort: Int = 0
    @JvmField
	var remoteAddress: String? = null
    @JvmField
	var remotePort: Int = 0
    @JvmField
	var local: Boolean = true

    @JvmField
	@Transient
    var exception: Exception? = null

    override fun toString(): String {
        val localName = "$localAddress:$localPort"
        val direction = if (local) "->" else "<-"
        val remoteName = "$remoteAddress:$remotePort"
        return "Tunnel ($localName$direction$remoteName)"
    }

    override fun compareTo(other: Tunnel): Int {
        var i = localAddress!!.compareTo(other.localAddress!!)
        if (i == 0) {
            i = localPort.compareTo(other.localPort)
        }
        return i
    }

    fun copy(): Tunnel {
        val copyTunnel = Tunnel()
        copyTunnel.local = local
        copyTunnel.localAddress = localAddress
        copyTunnel.localPort = localPort
        copyTunnel.remoteAddress = remoteAddress
        copyTunnel.remotePort = remotePort
        return copyTunnel
    }

    override fun hashCode(): Int {
        return Objects.hash(localAddress, localPort)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val other0: Tunnel = other as Tunnel
        return localAddress == other0.localAddress && localPort == other0.localPort
    }
}
