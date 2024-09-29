package com.kct.iqsdisplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import com.bumptech.glide.Glide
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentReserveCallBinding

class FragmentReserveCall : Fragment() {

    private var _binding: FragmentReserveCallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReserveCallBinding.inflate(inflater, container, false)
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
        binding.tvDeskNum.text       = getString(R.string.format_two_digit).format(ScreenInfo.winNum)
        binding.tvDeskName.text      = ScreenInfo.getWinName(ScreenInfo.winId)
        binding.ivTellerImg.setTellerImage()
        binding.tvTellerName.text   = ScreenInfo.tellerInfo.tellerName

        ScreenInfo.reserveCallInfo.observe(viewLifecycleOwner) { reserveCallInfo ->
            val customerName = with(reserveCallInfo.customerName) {
                substring(0, lastIndex) + "*"
            }
            binding.tvCallNum.text = getString(R.string.format_four_digit).format(reserveCallInfo.reserveCallNum)
            binding.tvCustomerName.text = customerName
        }
    }

    private fun ImageView.setTellerImage() {
        val tellerImageFileName = ScreenInfo.tellerInfo.tellerImg
        val serverIp = Const.ConnectionInfo.IQS_IP

        Glide.with(requireContext())
            .load("http://$serverIp/image/staff/$tellerImageFileName")
            .fitCenter()
            .error("${Const.Path.DIR_IMAGE}${Const.Name.DEFAULT_TELLER_IMAGE}")
            .into(this)
    }

}