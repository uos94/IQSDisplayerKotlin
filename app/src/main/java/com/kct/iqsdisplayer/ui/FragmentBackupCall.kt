package com.kct.iqsdisplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentBackupCallBinding

/**
 * 화면을 본적이 없어서 수정을 못함.
 */
class FragmentBackupCall : Fragment() {
    private var _binding: FragmentBackupCallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupCallBinding.inflate(inflater, container, false)
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
        binding.ivBackupArrow.setImageResource(
            if (ScreenInfo.instance.callBkWay == 1) {
                R.drawable.background_backup_left
            } else {
                R.drawable.background_backup_right
            }
        )

        binding.tvBackupWinNum.text = ScreenInfo.instance.callWinNum.toString()

        val digitFormat = getString(R.string.format_four_digit)
        ScreenInfo.instance.callNum.observe(viewLifecycleOwner) {
            binding.tvBackupCallNum.text  = digitFormat.format(it)
        }

        //창구ID 같으면 해당ID의 창구명 설정
        ScreenInfo.instance.winList.find { it.winID == ScreenInfo.instance.ticketWinID }?.let {
            binding.tvBackupWinName.text = it.winName
        }
    }
}