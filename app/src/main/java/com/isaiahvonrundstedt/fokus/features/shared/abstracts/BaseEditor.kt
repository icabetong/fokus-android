package com.isaiahvonrundstedt.fokus.features.shared.abstracts

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.isaiahvonrundstedt.fokus.R

abstract class BaseEditor: BaseFragment() {

    override fun onStart() {
        super.onStart()

        with(requireActivity().window) {
            statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ContextCompat.getColor(requireContext(), R.color.color_window_background)
            else Color.BLACK

            navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
                ContextCompat.getColor(requireContext(), R.color.color_navigation_background)
            else Color.BLACK

            if (!isThemeDark) {
                setLightStatusBarCompat()
                setLightNavigationBarCompat()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        with(requireActivity().window) {
            statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ContextCompat.getColor(requireContext(), R.color.color_status_background)
            else Color.BLACK

            navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
                ContextCompat.getColor(requireContext(), R.color.color_navigation_background)
            else Color.BLACK
        }
    }

    private val isThemeDark: Boolean
        get() = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    protected val animation: Animation
        get() = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fade_in)

    @Suppress("DEPRECATION")
    private fun setLightStatusBarCompat() {
        with(requireActivity().window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.run {
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            } else return
        }
    }

    @Suppress("DEPRECATION")
    private fun setLightNavigationBarCompat() {
        with(requireActivity().window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                decorView.run {
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else return
        }
    }

}