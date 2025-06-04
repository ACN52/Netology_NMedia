package ru.netology.nmedia.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

object AndroidUtils {

    // Скрыть клавиатуру для Activity
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Скрыть клавиатуру для Fragment
    fun hideKeyboard(fragment: Fragment) {
        fragment.activity?.let { hideKeyboard(it) }
    }

    // Скрыть клавиатуру для View
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}