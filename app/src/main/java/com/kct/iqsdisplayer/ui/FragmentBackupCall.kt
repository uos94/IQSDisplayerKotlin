package com.kct.iqsdisplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
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
        ScreenInfo.backupCallInfo.observe(viewLifecycleOwner) {
            binding.tvCallNum.text    = getString(R.string.format_four_digit).format(it.callNum)
            binding.tvDeskName.text   = it.backupWinName
            binding.tvDeskNum.text    = it.backupWinNum.toString()
            if (it.bkWay == Const.Arrow.LEFT) {
                binding.vArrowLeft.visibility  = View.VISIBLE
                binding.vArrowRight.visibility = View.GONE
            }
            else {
                binding.vArrowLeft.visibility  = View.GONE
                binding.vArrowRight.visibility = View.VISIBLE
            }
        }
    }
}