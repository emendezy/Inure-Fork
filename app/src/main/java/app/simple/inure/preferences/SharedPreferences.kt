package app.simple.inure.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.simple.inure.util.NullSafety.isNull
import java.io.File

object SharedPreferences {

    private const val preferences = "Preferences"
    private const val preferencesEncrypted = "PreferencesSecured"
    private var sharedPreferences: SharedPreferences? = null
    private var encryptedSharedPreferences: SharedPreferences? = null

    private var isInitialized = false

    fun init(context: Context) {
        if (sharedPreferences.isNull()) {
            sharedPreferences = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
        }
    }

    fun SharedPreferences.nullify() {
        sharedPreferences = null
    }

    fun initEncrypted(context: Context) {
        kotlin.runCatching {
            if (encryptedSharedPreferences.isNull()) {
                val masterKeyAlias = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                encryptedSharedPreferences = EncryptedSharedPreferences.create(
                        context,
                        preferencesEncrypted,
                        masterKeyAlias,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
            }
        }.onFailure {
            /**
             * Retry with a fail safe for recursion
             */
            if (isInitialized.not()) {
                // Delete the encrypted shared preferences if it fails to initialize
                File(getEncryptedSharedPreferencesPath(context)).delete()

                init(context)
                isInitialized = true
            }
        }
    }

    /**
     * Singleton to hold reference of SharedPreference.
     * Call [init] first before making a instance request
     *
     * @see init
     * @throws NullPointerException
     */
    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences ?: throw NullPointerException()
    }

    fun getEncryptedSharedPreferences(): SharedPreferences {
        return encryptedSharedPreferences ?: throw NullPointerException()
    }

    fun registerSharedPreferencesListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Use this function to register shared preference change listener if
     * the current context has [SharedPreferences.OnSharedPreferenceChangeListener]
     * implemented.
     */
    fun SharedPreferences.OnSharedPreferenceChangeListener.registerSharedPreferenceChangeListener() {
        registerSharedPreferencesListener(this)
    }

    /**
     * Use this function to unregister shared preference change listener if
     * the current context has [SharedPreferences.OnSharedPreferenceChangeListener]
     * implemented.
     */
    fun SharedPreferences.OnSharedPreferenceChangeListener.unregisterSharedPreferenceChangeListener() {
        unregisterListener(this)
    }

    /**
     * Singleton to hold reference of SharedPreference.
     *
     * @see init
     */
    fun getSharedPreferences(context: Context): SharedPreferences {
        kotlin.runCatching {
            return sharedPreferences ?: throw NullPointerException()
        }.getOrElse {
            init(context)
            return sharedPreferences!!
        }
    }

    fun getSharedPreferencesPath(context: Context): String {
        return context.applicationInfo.dataDir + "/shared_prefs/" + preferences + ".xml"
    }

    fun getEncryptedSharedPreferencesPath(context: Context): String {
        return context.applicationInfo.dataDir + "/shared_prefs/" + preferencesEncrypted + ".xml"
    }
}