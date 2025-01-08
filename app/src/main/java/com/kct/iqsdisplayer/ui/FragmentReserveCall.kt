package com.kct.iqsdisplayer.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
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
        ScreenInfo.tellerData.observe(viewLifecycleOwner) {
            binding.tvDeskNum.text       = getString(R.string.format_two_digit).format(ScreenInfo.winNum)
            binding.tvDeskName.text      = ScreenInfo.getWinName(ScreenInfo.winId)

            binding.ivTellerImg.setTellerImage()
            binding.tvTellerName.text   = it.tellerName
        }

        ScreenInfo.reserveCallInfo.observe(viewLifecycleOwner) { reserveCallInfo ->
            binding.tvCallNum.text = getString(R.string.format_four_digit).format(reserveCallInfo.reserveCallNum)
            binding.tvCustomerName.text = reserveCallInfo.customerName

            val colorAnim = ObjectAnimator.ofInt(
                binding.tvCallNum, // 애니메이션을 적용할 TextView 객체
                "textColor", // 변경할 속성 이름
                Color.BLACK,
                Color.BLUE
            ).apply {
                duration = 1000 // 1초 동안 색상 변경
                setEvaluator(ArgbEvaluator())
                repeatCount = 5 // 총 6번 변경 (시작 시 검은색, 5번 반복)
                repeatMode = ValueAnimator.REVERSE
            }
            // 애니메이션 종료 시 텍스트 색상을 검은색으로 고정
            colorAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.tvCallNum.setTextColor(Color.BLACK)
                }
            })
            colorAnim.start()
        }
    }

    private fun ImageView.setTellerImage() {
        val tellerImageFileName = ScreenInfo.tellerData.value?.tellerImg ?: ""

        val imgPath = if(tellerImageFileName.isEmpty()) "${Const.Path.DIR_IMAGE}${Const.Name.DEFAULT_TELLER_IMAGE}"
        else "${Const.Path.DIR_TELLER_IMAGE}$tellerImageFileName"

        Glide.with(requireContext())
            .load("${Const.Path.DIR_TELLER_IMAGE}$tellerImageFileName")
            .signature(ObjectKey(System.currentTimeMillis().toString()))
            .fitCenter()
            .error("${Const.Path.DIR_IMAGE}${Const.Name.DEFAULT_TELLER_IMAGE}")
            .into(this)
    }

}