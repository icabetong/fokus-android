package com.isaiahvonrundstedt.fokus.features.event

import android.os.Bundle
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import kotlinx.android.synthetic.main.layout_appbar.*

class EventActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        setPersistentActionBar(toolbar, R.string.activity_events)
    }

}