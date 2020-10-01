package com.isaiahvonrundstedt.fokus.features.attachments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseBottomSheet
import com.isaiahvonrundstedt.fokus.features.shared.adapters.MenuAdapter
import kotlinx.android.synthetic.main.layout_sheet_options.*

class AttachmentOptionSheet(manager: FragmentManager): BaseBottomSheet<Int>(manager),
    MenuAdapter.MenuItemListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_sheet_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuTitleView.text = getString(R.string.dialog_new_attachment)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MenuAdapter(activity, R.menu.menu_attachment, this)
    }

    override fun onItemSelected(id: Int) {
        receiver?.onReceive(id)
    }
}