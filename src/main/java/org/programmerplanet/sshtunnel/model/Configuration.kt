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

import org.programmerplanet.sshtunnel.util.createKeyString
import org.programmerplanet.sshtunnel.util.decrypt
import org.programmerplanet.sshtunnel.util.encrypt
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Responsible for storing and loading the configuration for the application.
 *
 * @author [Joseph Fifield](jfifield@programmerplanet.org)
 */
class Configuration {
    var top: Int = 0
    var left: Int = 0
    var width: Int = 500
    var height: Int = 400
    var weights: IntArray = intArrayOf(5, 7)
    private val sessions: MutableList<Session> = ArrayList()

    fun getSessions(): MutableList<Session> {
        return sessions
    }

    @Throws(IOException::class)
    fun write() {
        val properties = Properties()

        properties.setProperty("top", top.toString())
        properties.setProperty("left", left.toString())
        properties.setProperty("width", width.toString())
        properties.setProperty("height", height.toString())
        properties.setProperty("weights", intArrayToString(this.weights))

        val si: ListIterator<Session> = sessions.listIterator()
        while (si.hasNext()) {
            val session = si.next()

            val sessionKey = "sessions[" + si.previousIndex() + "]"

            properties.setProperty("$sessionKey.sessionName", session.sessionName)
            properties.setProperty("$sessionKey.hostname", session.hostname)
            properties.setProperty("$sessionKey.port", session.port.toString())
            properties.setProperty("$sessionKey.username", session.username)
            if (session.password != null) {
                val keyString = createKeyString()
                val encryptedPassword = encrypt(session.password!!, keyString!!)
                properties.setProperty("$sessionKey.key", keyString)
                properties.setProperty("$sessionKey.password", encryptedPassword)
            }

            if (session.identityPath != null) {
                properties.setProperty("$sessionKey.identityPath", session.identityPath)
            }

            if (session.passPhrase != null) {
                val keyString = createKeyString()
                val encryptedPassphrase = encrypt(session.passPhrase!!, keyString!!)
                properties.setProperty("$sessionKey.passphraseKey", keyString)
                properties.setProperty("$sessionKey.passphrase", encryptedPassphrase)
            }

            if (session.ciphers != null) {
                properties.setProperty("$sessionKey.ciphers", session.ciphers)
            }

            if (session.debugLogPath != null) {
                properties.setProperty("$sessionKey.debugLogPath", session.debugLogPath)
            }

            properties.setProperty("$sessionKey.compression", session.isCompressed.toString())

            val ti: ListIterator<Tunnel> = session.tunnels.listIterator()
            while (ti.hasNext()) {
                val tunnel = ti.next()

                val tunnelKey = sessionKey + ".tunnels[" + ti.previousIndex() + "]"

                properties.setProperty("$tunnelKey.localAddress", tunnel.localAddress)
                properties.setProperty("$tunnelKey.localPort", tunnel.localPort.toString())
                properties.setProperty("$tunnelKey.remoteAddress", tunnel.remoteAddress)
                properties.setProperty("$tunnelKey.remotePort", tunnel.remotePort.toString())
                properties.setProperty("$tunnelKey.local", tunnel.local.toString())
            }
        }

        storeProperties(properties)
    }

    @Throws(IOException::class)
    fun read() {
        val properties = loadProperties()

        this.top = properties.getProperty("top", top.toString()).toInt()
        this.left = properties.getProperty("left", left.toString()).toInt()
        this.width = properties.getProperty("width", width.toString()).toInt()
        this.height = properties.getProperty("height", height.toString()).toInt()
        this.weights = stringToIntArray(properties.getProperty("weights", intArrayToString(this.weights)))

        var sessionIndex = 0
        var moreSessions = true
        while (moreSessions) {
            val sessionKey = "sessions[$sessionIndex]"

            val sessionName = properties.getProperty("$sessionKey.sessionName")
            moreSessions = (sessionName != null)
            if (moreSessions) {
                val hostname = properties.getProperty("$sessionKey.hostname")
                val port = properties.getProperty("$sessionKey.port")
                val username = properties.getProperty("$sessionKey.username")

                val keyString = properties.getProperty("$sessionKey.key")
                var password = properties.getProperty("$sessionKey.password")
                if (keyString != null && password != null) {
                    password = decrypt(password, keyString)
                }

                val identityPath = properties.getProperty("$sessionKey.identityPath")

                val passphraseKeyString = properties.getProperty("$sessionKey.passphraseKey")
                var passphrase = properties.getProperty("$sessionKey.passphrase")
                if (passphraseKeyString != null && passphrase != null) {
                    passphrase = decrypt(passphrase, passphraseKeyString)
                }


                val session = Session(sessionName)
                session.sessionName = sessionName
                session.hostname = hostname
                if (port != null && port.isNotEmpty()) {
                    session.port = port.toInt()
                }
                session.username = username
                session.password = password
                session.identityPath = identityPath
                session.passPhrase = passphrase

                val ciphers = properties.getProperty("$sessionKey.ciphers")
                session.ciphers = ciphers

                val debugLogPath = properties.getProperty("$sessionKey.debugLogPath")
                if (debugLogPath != null) {
                    session.debugLogPath = debugLogPath
                }

                var compressed = false
                val compressionString = properties.getProperty("$sessionKey.compression")
                if (compressionString != null) {
                    compressed = compressionString.toBoolean()
                }
                session.isCompressed = compressed

                sessions.add(session)

                var tunnelIndex = 0
                var moreTunnels = true
                while (moreTunnels) {
                    val tunnelKey = "$sessionKey.tunnels[$tunnelIndex]"

                    val localAddress = properties.getProperty("$tunnelKey.localAddress")
                    moreTunnels = (localAddress != null)
                    if (moreTunnels) {
                        val localPort = properties.getProperty("$tunnelKey.localPort")
                        val remoteAddress = properties.getProperty("$tunnelKey.remoteAddress")
                        val remotePort = properties.getProperty("$tunnelKey.remotePort")
                        var local = true
                        val localConf = properties.getProperty("$tunnelKey.local")
                        if (localConf != null) {
                            local = localConf.toBoolean()
                        }

                        val tunnel = Tunnel()
                        tunnel.localAddress = localAddress
                        tunnel.localPort = localPort.toInt()
                        tunnel.remoteAddress = remoteAddress
                        tunnel.remotePort = remotePort.toInt()
                        tunnel.local = local

                        session.tunnels.add(tunnel)
                    }
                    tunnelIndex++
                }
            }
            sessionIndex++
        }
    }

    /**
     * Loads the properties from the user's configuration file.
     */
    @Throws(IOException::class)
    private fun loadProperties(): Properties {
        val properties = Properties()

        val configFile = configurationFile
        if (configFile.exists()) {
            FileInputStream(configFile).use { fis ->
                properties.load(fis)
            }
        }

        return properties
    }

    /**
     * Stores the properties to the user's configuration file.
     */
    @Throws(IOException::class)
    private fun storeProperties(properties: Properties) {
        val configFile = configurationFile

        FileOutputStream(configFile).use { fos ->
            properties.store(fos, "SSH Tunnel Configuration")
        }
    }

    private val configurationFile: File
        /**
         * Gets the configuration file (a file named .sshtunnel in the user's
         * home directory).
         */
        get() {
            val userDir = System.getProperty("user.home")
            return File(userDir, ".sshtunnel")
        }

    /**
     * Converts an int array into a comma-delimited string.
     */
    private fun intArrayToString(intArray: IntArray): String {
        var str = intArray.contentToString()
        str = str.replace("[\\[\\] ]".toRegex(), "")
        return str
    }

    /**
     * Converts a comma-delimited string into an int array.
     */
    private fun stringToIntArray(str: String): IntArray {
        val strArray = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val intArray = IntArray(strArray.size)
        for (i in strArray.indices) {
            intArray[i] = strArray[i].toInt()
        }
        return intArray
    }
}
