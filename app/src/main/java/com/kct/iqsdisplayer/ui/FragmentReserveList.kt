package com.kct.iqsdisplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentReserveListBinding

class FragmentReserveList : Fragment() {

    private var _binding: FragmentReserveListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReserveListBinding.inflate(inflater, container, false)
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
        val row1 = binding.row1
        val row2 = binding.row2
        val row3 = binding.row3
        val row4 = binding.row4
        val reserveItems = arrayOf(
            ReserveListItem(row1.llViewGroup, row1.tvCustomerName, row1.tvReserveTime, row1.tvTellerName, row1.tvReserveNum),
            ReserveListItem(row2.llViewGroup, row2.tvCustomerName, row2.tvReserveTime, row2.tvTellerName, row2.tvReserveNum),
            ReserveListItem(row3.llViewGroup, row3.tvCustomerName, row3.tvReserveTime, row3.tvTellerName, row3.tvReserveNum),
            ReserveListItem(row4.llViewGroup, row4.tvCustomerName, row4.tvReserveTime, row4.tvTellerName, row4.tvReserveNum)
        )

        // reserveList 의 마지막 4개를 가져옴
        ScreenInfo.reserveList.takeLast(4).forEachIndexed { index, reserve ->

            val digitFormat = getString(R.string.format_four_digit)

            reserveItems[index].viewGroup.visibility = View.VISIBLE
            reserveItems[index].tvCustomerName.text  = reserve.customerName
            reserveItems[index].tvReserveTime.text   = reserve.reserveTime
            reserveItems[index].tvTellerName.text    = reserve.tellerName
            reserveItems[index].tvReserveNum.text    = digitFormat.format(reserve.reserveNum)
        }
        // 나머지 callItems는 숨김 처리
        for (index in ScreenInfo.reserveList.size until reserveItems.size) {
            reserveItems[index].viewGroup.visibility = View.INVISIBLE
        }
    }

    inner class ReserveListItem(
        val viewGroup: LinearLayout,
        val tvCustomerName: TextView,
        val tvReserveTime: TextView,
        val tvTellerName: TextView,
        val tvReserveNum: TextView
    )

}