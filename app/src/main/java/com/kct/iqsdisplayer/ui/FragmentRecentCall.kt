package com.kct.iqsdisplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentRecentCallBinding
import com.kct.iqsdisplayer.databinding.ItemCallMiddleBinding
import com.kct.iqsdisplayer.databinding.ItemCallSmallBinding

class FragmentRecentCall : Fragment() {

    private var _binding: FragmentRecentCallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUIData()
    }

    private fun setUIData() {
        val callItemBindings = arrayOf(
            ItemCallMiddleBinding.bind(binding.clCallItem1.root),
            ItemCallSmallBinding.bind(binding.clCallItem2.root),
            ItemCallSmallBinding.bind(binding.clCallItem3.root),
            ItemCallSmallBinding.bind(binding.clCallItem4.root))

        ScreenInfo.instance.lastCallList.observe(viewLifecycleOwner) { lastCallList ->
            // lastCallList 의 마지막 4개를 가져옴
            val recentCalls = lastCallList.takeLast(4)

            val digitFormat = getString(R.string.format_four_digit)
            callItemBindings.forEachIndexed { index, itemBinding ->
                if (index < recentCalls.size) {
                    val lastCall = recentCalls[index]
                    when(itemBinding) {
                        is ItemCallMiddleBinding -> {
                            itemBinding.tvWinNum.text = if(lastCall.callWinNum <= 0) "" else lastCall.callWinNum.toString()
                            itemBinding.tvCallNum.text = if(lastCall.callNum <= 0) "" else digitFormat.format(lastCall.callNum)
                        }
                        is ItemCallSmallBinding -> {
                            itemBinding.tvWinNum.text = if(lastCall.callWinNum <= 0) "" else lastCall.callWinNum.toString()
                            itemBinding.tvCallNum.text = if(lastCall.callNum <= 0) "" else digitFormat.format(lastCall.callNum)
                        }
                    }
                    itemBinding.root.visibility = View.VISIBLE
                } else {
                    itemBinding.root.visibility = View.INVISIBLE
                }
            }
        }
    }

    enum class CallItemIndex {
        ITEM_1, ITEM_2, ITEM_3, ITEM_4
    }
}