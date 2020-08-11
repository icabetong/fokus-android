package com.isaiahvonrundstedt.fokus.components.extensions.android

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun AppCompatActivity.createSnackbar(@StringRes textRes: Int,
                                     view: View = window.decorView.rootView,
                                     duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
    return Snackbar.make(view, getString(textRes), duration).apply { show() }
}

fun AppCompatActivity.createToast(@StringRes textRes: Int,
                                  duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, getString(textRes), duration).apply { show() }
}

fun Fragment.createSnackbar(@StringRes textRes: Int,
                            view: View = this.requireView(),
                            duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
    return Snackbar.make(view, getString(textRes), duration).apply { show() }
}

fun Fragment.createToast(@StringRes textRes: Int,
                         duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(requireContext(), getString(textRes), duration).apply { show() }
}