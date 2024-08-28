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
 * Represents a tunnel (port forward) over an ssh connection.
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 * @author [Mulya Agung](agungm@outlook.com)
 */
data class Tunnel(
    var localAddress: String? = null,
    var localPort: Int = 0,
    var remoteAddress: String? = null,
    var remotePort: Int = 0,
    var local: Boolean = true
) : Comparable<Tunnel> {

    @Transient
    var exception: Exception? = null

    override fun toString(): String {
        val localName = "$localAddress:$localPort"
        val direction = if (local) "->" else "<-"
        val remoteName = "$remoteAddress:$remotePort"
        return "Tunnel ($localName$direction$remoteName)"
    }

    override fun compareTo(other: Tunnel): Int {
        return compareValuesBy(this, other,
            { it.localAddress ?: "" },
            { it.localPort }
        )
    }

}
