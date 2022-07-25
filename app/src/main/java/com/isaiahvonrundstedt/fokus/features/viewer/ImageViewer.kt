package com.isaiahvonrundstedt.fokus.features.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import coil.load
import com.isaiahvonrundstedt.fokus.databinding.LayoutViewerImageBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseViewerFragment
import java.io.File

class ImageViewer(manager: FragmentManager) : BaseViewerFragment(manager) {
    private var _binding: LayoutViewerImageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutViewerImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.appBarLayout.toolbar) {
            setNavigationOnClickListener { dismiss() }
            title = arguments?.getString(EXTRA_TITLE) ?: arguments?.getString(EXTRA_IMAGE_PATH)
        }

        arguments?.getString(EXTRA_IMAGE_PATH)?.let {
            binding.imageContainer.load(File(it))
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra:title"
        private const val EXTRA_IMAGE_PATH = "extra:path"

        fun show(manager: FragmentManager, path: String, title: String? = null) {
            ImageViewer(manager).apply {
                arguments = bundleOf(
                    EXTRA_IMAGE_PATH to path,
                    EXTRA_TITLE to title
                )
            }.show()
        }
    }
}