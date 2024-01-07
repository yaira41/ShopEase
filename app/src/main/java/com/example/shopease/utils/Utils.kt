package com.example.shopease.utils

import java.security.MessageDigest

object Utils {

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return bytesToHex(digest)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    fun byteArrayToBase64(byteArray: ByteArray?): String {
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }

    fun base64ToByteArray(base64String: String): ByteArray {
        return android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
    }
}
