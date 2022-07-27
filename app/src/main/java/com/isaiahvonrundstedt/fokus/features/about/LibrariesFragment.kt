package com.isaiahvonrundstedt.fokus.features.about

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.databinding.FragmentLibrariesBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutItemLibraryBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library

class LibrariesFragment : BaseFragment() {
    private var _binding: FragmentLibrariesBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibrariesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBarLayout.toolbar, arrayOf(binding.recyclerView))
        controller = Navigation.findNavController(view)

        with(binding.appBarLayout.toolbar) {
            setTitle(R.string.activity_open_source_licenses)
            setupNavigation(this, R.drawable.ic_outline_arrow_back_24) {
                controller?.navigateUp()
            }
        }

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = LibraryAdapter(Libs(context).libraries)
        }
    }

    class LibraryAdapter(private val itemList: List<Library>) :
        RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
            val rowView: View = LayoutInflater.from(parent.context).inflate(
                R.layout.layout_item_library,
                parent, false
            )
            return LibraryViewHolder(rowView)
        }

        override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
            holder.onBind(itemList[position])
        }

        override fun getItemCount(): Int = itemList.size

        class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val binding = LayoutItemLibraryBinding.bind(itemView)

            @Suppress("DEPRECATION")
            fun onBind(library: Library) {

                binding.nameTextView.text = library.libraryName
                binding.versionTextView.text = library.libraryVersion

                if (library.author.isNotEmpty())
                    binding.authorTextView.text = library.author
                else binding.authorTextView.visibility = View.GONE

                val license = library.licenses?.firstOrNull()
                if (license != null) {
                    binding.licenseNameTextView.text = license.licenseName
                    binding.licenseDescriptionTextView.text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            Html.fromHtml(
                                license.licenseShortDescription,
                                Html.FROM_HTML_MODE_COMPACT
                            )
                        else Html.fromHtml(license.licenseShortDescription)
                } else {
                    binding.licenseNameTextView.visibility = View.GONE
                    binding.licenseDescriptionTextView.visibility = View.GONE
                }
            }
        }
    }
}