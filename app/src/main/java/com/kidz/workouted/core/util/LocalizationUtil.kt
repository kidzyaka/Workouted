package com.kidz.workouted.core.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kidz.workouted.R

object LocalizationUtil {
    fun getLocalizedName(context: Context, key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }
}

@Composable
fun localizedName(key: String): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else key
}
