package org.programmerplanet.sshtunnel.util

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock

object SingleInstanceChecker {
    private var lock: FileLock? = null

    fun isAppAlreadyRunning(): Boolean {
        val lockFile = File(System.getProperty("user.home"), ".yourapp.lock") // atau pakai File("/tmp/yourapp.lock") di Linux/Mac
        val channel = RandomAccessFile(lockFile, "rw").channel

        lock = try {
            channel.tryLock()
        } catch (e: Exception) {
            null
        }

        return lock == null
    }
}
