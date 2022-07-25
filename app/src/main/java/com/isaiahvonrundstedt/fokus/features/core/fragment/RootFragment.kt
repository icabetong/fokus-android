package com.isaiahvonrundstedt.fokus.features.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.databinding.FragmentRootBinding
import com.isaiahvonrundstedt.fokus.databinding.LayoutNavigationHeaderBinding
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import java.time.LocalTime

class RootFragment: BaseFragment() {
    private var _binding: FragmentRootBinding? = null
    private var _headerBinding: LayoutNavigationHeaderBinding? = null
    private var controller: NavController? = null
    private var mainController: NavController? = null

    private val binding get() = _binding!!
    private val headerBinding get() = _headerBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        _headerBinding = null
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainController = requireActivity().supportFragmentManager
            .findFragmentById(R.id.navigationHostFragment)?.findNavController()

        /**
         *  Get this fragment's NavController
         *  to control what fragment will show up
         *  depending on what directions is on the
         *  NavigationViewModel
         */
        controller = (childFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as? NavHostFragment)?.navController
        if (binding.navigationView.headerCount > 0) {
            _headerBinding = LayoutNavigationHeaderBinding
                .bind(binding.navigationView.getHeaderView(0))
            headerBinding.menuTitleView.text = when (LocalTime.now().hour) {
                in 0..6 -> getString(R.string.greeting_default)
                in 7..12 -> getString(R.string.greeting_morning)
                in 13..18 -> getString(R.string.greeting_afternoon)
                in 19..23 -> getString(R.string.greeting_evening)
                else -> getString(R.string.greeting_default)
            }
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onResume() {
        super.onResume()

        binding.navigationView.setNavigationItemSelectedListener {
            binding.navigationView.setCheckedItem(it.itemId)
            try {
                controller?.navigate(it.itemId)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } catch (exception: Exception) {
                mainController?.navigate(it.itemId)
            }
            true
        }
    }
}