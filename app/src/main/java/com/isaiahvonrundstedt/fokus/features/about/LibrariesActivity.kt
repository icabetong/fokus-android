package com.isaiahvonrundstedt.fokus.features.about

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.CoreApplication
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import kotlinx.android.synthetic.main.activity_libraries.*
import kotlinx.android.synthetic.main.layout_appbar.*

class LibrariesActivity: BaseActivity() {

    private val adapter = LibraryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_libraries)
        setPersistentActionBar(toolbar)
        setToolbarTitle(R.string.activity_open_source_licenses)

        adapter.setItems(Libs(this).libraries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    class LibraryAdapter: RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

        private val itemList = mutableListOf<Library>()

        fun setItems(items: List<Library>) {
            itemList.clear()
            itemList.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
            val rowView: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_library,
                parent, false)
            return LibraryViewHolder(rowView)
        }

        override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
            holder.onBind(itemList[position])
        }

        override fun getItemCount(): Int = itemList.size

        class LibraryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
            private val versionTextView: TextView = itemView.findViewById(R.id.versionTextView)
            private val licenseNameTextView: TextView = itemView.findViewById(R.id.licenseNameTextView)
            private val licenseDescriptionTextView: TextView = itemView.findViewById(R.id.licenseDescriptionTextView)

            @Suppress("DEPRECATION")
            fun onBind(library: Library) {

                nameTextView.text = library.libraryName
                versionTextView.text = library.libraryVersion

                if (library.author.isNotEmpty())
                    authorTextView.text = library.author
                else authorTextView.visibility = View.GONE

                val license = library.licenses?.firstOrNull()
                if (license != null) {
                    licenseNameTextView.text = license.licenseName
                    licenseDescriptionTextView.text =
                        if (CoreApplication.isRunningAtVersion(Build.VERSION_CODES.N))
                            Html.fromHtml(license.licenseShortDescription, Html.FROM_HTML_MODE_COMPACT)
                        else Html.fromHtml(license.licenseShortDescription)
                } else {
                    licenseNameTextView.visibility = View.GONE
                    licenseDescriptionTextView.visibility = View.GONE
                }
            }
        }

    }
}