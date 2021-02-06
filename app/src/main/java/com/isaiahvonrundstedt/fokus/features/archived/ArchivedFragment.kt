package com.isaiahvonrundstedt.fokus.features.archived

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.databinding.FragmentArchivedBinding
import com.isaiahvonrundstedt.fokus.features.archived.event.ArchivedEventFragment
import com.isaiahvonrundstedt.fokus.features.archived.subject.ArchivedSubjectFragment
import com.isaiahvonrundstedt.fokus.features.archived.task.ArchivedTaskFragment
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseActivity
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.full.createInstance

@AndroidEntryPoint
class ArchivedFragment: BaseFragment() {
    private var _binding: FragmentArchivedBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentArchivedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                ViewPagerAdapter.fragments.indexOf(ArchivedTaskFragment::class) ->
                    tab.setText(R.string.navigation_tasks)
                ViewPagerAdapter.fragments.indexOf(ArchivedEventFragment::class) ->
                    tab.setText(R.string.navigation_events)
                ViewPagerAdapter.fragments.indexOf(ArchivedSubjectFragment::class) ->
                    tab.setText(R.string.navigation_subjects)
            }
        }.attach()
    }

    class ViewPagerAdapter(parentFragment: Fragment): FragmentStateAdapter(parentFragment) {
        companion object {
            val fragments = listOf(
                ArchivedTaskFragment::class,
                ArchivedEventFragment::class,
                ArchivedSubjectFragment::class
            )
        }

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position].createInstance()
        }
    }
}