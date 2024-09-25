package com.kct.iqsdisplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentRecentCallBinding

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
        val callItems = arrayOf(
            RecentCallItem(binding.clDesk1, binding.tvDeskNum1, binding.tvCallNum1),
            RecentCallItem(binding.clDesk2, binding.tvDeskNum2, binding.tvCallNum2),
            RecentCallItem(binding.clDesk3, binding.tvDeskNum3, binding.tvCallNum3),
            RecentCallItem(binding.clDesk4, binding.tvDeskNum4, binding.tvCallNum4)
        )

        ScreenInfo.lastCallList.observe(viewLifecycleOwner) { lastCallList ->
            // lastCallList 의 마지막 4개를 가져옴
            val recentCalls = lastCallList.takeLast(4)

            val digitFormat = getString(R.string.format_four_digit)

            // recentCalls를 역순으로 순회하며 callItems에 적용
            recentCalls.reversed().forEachIndexed { index, lastCall ->
                val item = callItems[index]
                item.viewGroup.visibility = View.VISIBLE
                item.tvWinNum.text = if(lastCall.callWinNum <= 0) "" else lastCall.callWinNum.toString()
                item.tvCallNum.text = if(lastCall.callNum <= 0) "" else digitFormat.format(lastCall.callNum)
            }

            // 나머지 callItems는 숨김 처리
            for (index in recentCalls.size until callItems.size) {
                callItems[index].viewGroup.visibility = View.INVISIBLE
            }
        }
    }

    inner class RecentCallItem(
        val viewGroup: ConstraintLayout, val tvWinNum: TextView, val tvCallNum: TextView
    )

}