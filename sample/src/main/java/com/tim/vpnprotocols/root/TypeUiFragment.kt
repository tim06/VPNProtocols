package com.tim.vpnprotocols.root

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.tim.vpnprotocols.R
import com.tim.vpnprotocols.databinding.TypeUiFragmentBinding

class TypeUiFragment : Fragment(R.layout.type_ui_fragment) {

    private val layoutBinding: TypeUiFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutBinding.viewPager.adapter = TypeTabAdapter()
        TabLayoutMediator(
            layoutBinding.tabLayout,
            layoutBinding.viewPager
        ) { tab, position ->
            tab.text = when (position) {
                0 -> "View"
                1 -> "Compose"
                else -> error("Unknown position")
            }
        }.attach()
    }

    private inner class TypeTabAdapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SelectProtocolViewFragment()
                1 -> SelectProtocolComposeFragment()
                else -> error("Unknown state")
            }
        }
    }
}