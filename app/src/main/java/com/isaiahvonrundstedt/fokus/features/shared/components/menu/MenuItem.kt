package com.isaiahvonrundstedt.fokus.features.shared.components.menu

import android.graphics.drawable.Drawable

data class MenuItem @JvmOverloads constructor (
    var id: Int = 0,
    var icon: Drawable? = null,
    var title: String? = null
)