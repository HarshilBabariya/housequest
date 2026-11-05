package com.homequest.app.util

import java.security.MessageDigest
import java.security.SecureRandom

object Pin {
    const val LENGTH = 4

    fun hash(pin: String, salt: String = newSalt()): String = "$salt:" + sha256("$salt:$pin")

    fun matches(pin: String, stored: String): Boolean {
        val salt = stored.substringBefore(':', "")
        return stored.isNotEmpty() && hash(pin, salt) == stored
    }

    fun isWeak(pin: String): Boolean = pin.toSet().size == 1 || pin in listOf("1234", "4321", "0123", "9876")

    private fun newSalt(): String = ByteArray(8).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
