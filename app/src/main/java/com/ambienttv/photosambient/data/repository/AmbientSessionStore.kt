package com.ambienttv.photosambient.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class AmbientSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
    val deviceId: String,
    val deviceName: String,
    val settingsUri: String?
)

/** Keeps the refresh token and device association encrypted on this TV. */
internal class AmbientSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("ambient_session", Context.MODE_PRIVATE)
    private val keyAlias = "ambient_gallery_session_v1"

    fun read(): AmbientSession? {
        val encoded = preferences.getString("session", null) ?: return null
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, 12)
            val ciphertext = bytes.copyOfRange(12, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            val json = JSONObject(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
            AmbientSession(
                accessToken = json.getString("accessToken"),
                refreshToken = json.getString("refreshToken"),
                expiresAtMillis = json.getLong("expiresAtMillis"),
                deviceId = json.getString("deviceId"),
                deviceName = json.getString("deviceName"),
                settingsUri = json.optString("settingsUri").ifBlank { null }
            )
        } catch (_: Exception) {
            clear()
            null
        }
    }

    fun write(session: AmbientSession) {
        val json = JSONObject()
            .put("accessToken", session.accessToken)
            .put("refreshToken", session.refreshToken)
            .put("expiresAtMillis", session.expiresAtMillis)
            .put("deviceId", session.deviceId)
            .put("deviceName", session.deviceName)
            .put("settingsUri", session.settingsUri ?: "")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        check(preferences.edit().putString("session", Base64.encodeToString(encrypted, Base64.NO_WRAP)).commit())
    }

    fun clear() {
        preferences.edit().remove("session").commit()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }
}
