package org.programmerplanet.sshtunnel.util

import java.io.IOException
import java.security.GeneralSecurityException
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

private const val ALGORITHM = "DES"

fun encrypt(source: String, keyString: String): String {
    val key = getKey(keyString)
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val clearBytes: ByteArray = source.toByteArray()
    val cipherBytes = cipher.doFinal(clearBytes)
    return byteArrayToHexString(cipherBytes)
}

fun decrypt(source: String, keyString: String): String {
    val key = getKey(keyString)
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, key)
    val cipherBytes = hexStringToByteArray(source)
    val clearBytes = cipher.doFinal(cipherBytes)
    return String(clearBytes)
}

fun createKeyString(): String {
    val key = createKey()
    val bytes = key.encoded
    return byteArrayToHexString(bytes)
}

@Throws(GeneralSecurityException::class)
private fun createKey(): Key {
    val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
    return keyGenerator.generateKey()
}

@Throws(IOException::class, GeneralSecurityException::class)
private fun getKey(keyString: String): Key {
    val bytes = hexStringToByteArray(keyString)
    val keySpec = DESKeySpec(bytes)
    val keyFactory = SecretKeyFactory.getInstance(ALGORITHM)
    return keyFactory.generateSecret(keySpec)
}

private fun byteArrayToHexString(bytes: ByteArray): String {
    val buffer = StringBuilder(bytes.size * 2)
    for (b in bytes) {
        val value = b.toInt() and 0xff
        if (value < 16) {
            buffer.append('0')
        }
        val s = Integer.toHexString(value)
        buffer.append(s)
    }
    return buffer.toString().uppercase(Locale.getDefault())
}

@Throws(IOException::class)
private fun hexStringToByteArray(str: String): ByteArray {
    val bytes = ByteArray(str.length / 2)
    for (i in bytes.indices) {
        val index: Int = i * 2
        val s: String = str.substring(index, index + 2)
        val value: Int = s.toInt(16)
        bytes[i] = value.toByte()
    }
    return bytes
}
