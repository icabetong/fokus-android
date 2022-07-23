package com.isaiahvonrundstedt.fokus.features.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.databinding.FragmentMainBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment() {
    private var _binding: FragmentMainBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = Navigation.findNavController(view)
        setupNavigation(binding.appBarLayout.toolbar, controller)

        /**
         * Enables the return transition between the inner
         * fragments.
         */
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val navigationHost = childFragmentManager.findFragmentById(R.id.nestedNavigationHost)
        navigationHost?.findNavController()?.also {
            binding.navigationView.setupWithNavController(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}