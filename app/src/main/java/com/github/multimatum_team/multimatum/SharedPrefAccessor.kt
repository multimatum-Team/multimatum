package com.github.multimatum_team.multimatum

import android.content.SharedPreferences

interface SharedPrefAccessor {

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, value: Boolean): Boolean

    companion object {
        fun of(sharedPreferences: SharedPreferences): SharedPrefAccessor =
            SharedPrefAccessorImpl(sharedPreferences)
    }

    private class SharedPrefAccessorImpl(private val underlyingSharedPref: SharedPreferences): SharedPrefAccessor {

        override fun putBoolean(key: String, value: Boolean) {
            val edit = underlyingSharedPref.edit()
            edit.putBoolean(key, value)
            edit.apply()
        }

        override fun getBoolean(key: String, value: Boolean): Boolean =
            underlyingSharedPref.getBoolean(key, value)
    }

}