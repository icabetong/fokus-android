package com.isaiahvonrundstedt.fokus.components.extensions.android

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<ArrayList<T>>.notifyObservers() {
    this.value = this.value
}