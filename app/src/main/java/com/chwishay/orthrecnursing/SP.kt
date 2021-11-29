package com.chwishay.orthrecnursing

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

private val defaultSP by lazy {
    SP("default")
}

class SP(private val name: String) :
    SharedPreferences by OrthRecApp.instance.getSharedPreferences(name, Context.MODE_PRIVATE) {

    companion object : SharedPreferences by defaultSP {

        fun name(name: String): SP {
            return SP(name)
        }

    }

}

inline fun <reified T> SharedPreferences.get(key: String): T? {
    val json = getString(key, "")
    return if (!json.isNullOrBlank()) {
        Gson().fromJson(json, T::class.java)
    } else {
        null
    }
}

inline fun <reified T> SharedPreferences.put(key: String, data: T) {
    edit {
        putString(key, Gson().toJson(data))
    }
}