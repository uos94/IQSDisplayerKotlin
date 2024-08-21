package com.kct.iqsdisplayer.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfoManager
import com.kct.iqsdisplayer.databinding.FragmentSubBinding
import com.kct.iqsdisplayer.databinding.ItemCallBinding
import java.util.Locale

class FragmentSubScreen : Fragment() {
    private var _binding: FragmentSubBinding? = null
    private val binding get() = _binding!!

    //private lateinit var callItemBindings: Array<ItemCallBinding>

    private var mainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubBinding.inflate(inflater, container, false)
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
        //TODO : 기존코드는 서비스가 돌고 있지 않으면 '통신복구중'이라고 표시했으나 방법을 변경해야 할것으로 보인다.
        binding.tvWinName.text = if(ScreenInfoManager.instance.systemError != 0) getString(R.string.msg_network_error) else ""

        if(ScreenInfoManager.instance.theme == 0) {
            binding.root.background = ContextCompat.getDrawable(requireContext(), R.color.black)
        }
        if(Const.CommunicationInfo.CALLVIEW_MODE == "3") { // dyyoon 하나은행, 사운드 디스플레이일 경우 대기명 보이지 않음
            binding.tvWaitNum.visibility = TextView.INVISIBLE
        }
        else {
            binding.tvWaitNum.text = getString(R.string.format_wait_num).format(ScreenInfoManager.instance.waitNum)
        }

        val callItemBindings = arrayOf(
            ItemCallBinding.bind(binding.clCallItem1.root),
            ItemCallBinding.bind(binding.clCallItem2.root),
            ItemCallBinding.bind(binding.clCallItem3.root),
            ItemCallBinding.bind(binding.clCallItem4.root))

        ScreenInfoManager.instance.subList.observe(viewLifecycleOwner) { subList ->
            // subList 의 마지막 4개를 가져옴
            val recentCalls = subList.takeLast(4)

            val digitFormat = getString(R.string.format_four_digit)
            //clCallItem1 부터 차례 대로 반영함
            callItemBindings.forEachIndexed { index, itemBinding ->
                if (index < recentCalls.size) {
                    val lastCall = recentCalls[index]
                    itemBinding.tvWinNum.text = if(lastCall.callWinNum <= 0) "" else lastCall.callWinNum.toString()
                    itemBinding.tvCallNum.text = if(lastCall.callNum <= 0) "" else digitFormat.format(lastCall.callNum)
                    itemBinding.root.visibility = View.VISIBLE
                } else {
                    itemBinding.root.visibility = View.INVISIBLE
                }
            }
        }
    }
}