package com.github.multimatum_team.multimatum

import android.content.SharedPreferences

interface MockSharedPreferencesBase: SharedPreferences {

    override fun getAll(): MutableMap<String, *> = reportNotImplemented()

    override fun getString(key: String?, defValue: String?): String? = reportNotImplemented()

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        reportNotImplemented()

    override fun getInt(key: String?, defValue: Int): Int =
        reportNotImplemented()

    override fun getLong(key: String?, defValue: Long): Long = reportNotImplemented()

    override fun getFloat(key: String?, defValue: Float): Float = reportNotImplemented()

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = reportNotImplemented()

    override fun contains(key: String?): Boolean = reportNotImplemented()

    override fun edit(): SharedPreferences.Editor = reportNotImplemented()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        return reportNotImplemented()
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        return reportNotImplemented()
    }

    interface Editor: SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor =
            reportNotImplemented()

        override fun putStringSet(
            key: String?,
            values: MutableSet<String>?
        ): SharedPreferences.Editor =
            reportNotImplemented()

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor =
            reportNotImplemented()

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor =
            reportNotImplemented()

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor =
            reportNotImplemented()

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor =
            reportNotImplemented()

        override fun remove(key: String?): SharedPreferences.Editor =
            reportNotImplemented()

        override fun clear(): SharedPreferences.Editor =
            reportNotImplemented()

        override fun commit(): Boolean =
            reportNotImplemented()

        override fun apply() {
            return reportNotImplemented()
        }
    }

    companion object {
        private fun <T> reportNotImplemented(): T {
            throw NotImplementedError("called a function that is not implemented in MockSharedPreferences")
        }
    }

}