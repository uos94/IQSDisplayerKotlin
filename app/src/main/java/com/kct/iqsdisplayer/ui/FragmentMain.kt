package com.kct.iqsdisplayer.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfoManager
import com.kct.iqsdisplayer.databinding.FragmentMainBinding
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.savePreference
import java.util.Locale

class FragmentMain : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var mainActivity: MainActivity? = null

    // infoMessage를 저장하는 MutableLiveData
    private val _infoMessage = MutableLiveData<String>()
    private val infoMessage: LiveData<String> get() = _infoMessage

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUIData()

        // SharedPreferences에 tellerMent 저장
        context?.savePreference(Const.Name.PREF_DISPLAY_INFO, Const.Key.DisplayInfo.STATUS_TEXT, ScreenInfoManager.instance.tellerMent)
    }


    private fun setUIData() {
        val screenInfo = ScreenInfoManager.instance

        binding.tvWinNum.text       = screenInfo.winNum.toString()
        binding.tvWinName.text      = screenInfo.winName

        binding.ivTellerImage.setTellerImage()
        binding.tvTellerName.text   = screenInfo.tellerName

        binding.tvWaitNum.text      = screenInfo.waitNum.toString()

        binding.tvInfo.text         = screenInfo.tellerMent
        infoMessage.observe(viewLifecycleOwner) { newInfoMessage ->
            binding.tvInfo.text = newInfoMessage
            val isErrorMessage = newInfoMessage == getString(R.string.network_error_msg) ||
                                 newInfoMessage == getString(R.string.system_error_msg)
            setMarqueeAnimation(isErrorMessage)
        }

        val emptyMsg = screenInfo.emptyMsg.ifEmpty { getString(R.string.default_absence_msg) }
        val callNumText = when {
            screenInfo.systemError == 1 -> getString(R.string.system_error_msg)
            screenInfo.pjt == 1 -> getString(R.string.vacancy_msg)
            screenInfo.flagEmpty == 1 -> emptyMsg
            else -> ""
        }
        setCallNumberText(callNumText)
    }

    fun setInfoText(infoMessage : String) {
        _infoMessage.value = infoMessage
    }

    private fun setMarqueeAnimation(isErrorMessage: Boolean) {
        if (isErrorMessage) {
            binding.tvInfo.clearAnimation()
            binding.tvInfo.gravity = Gravity.CENTER
        } else {
            val marquee = AnimationUtils.loadAnimation(activity, R.anim.marquee)
            binding.tvInfo.startAnimation(marquee)
            binding.tvInfo.gravity = Gravity.START
        }
    }

    /**
     * 호출 번호 또는 부재 메시지 설정
     * 1001, 부재중, 공  석 ..등
     */
    fun setCallNumberText(callNumberText: String) {
        val isAbsence = !callNumberText.all { it.isDigit() }
        binding.tvCallNum.textSize = if (isAbsence) 330f else 420f
        binding.tvCallNum.text = callNumberText

        if (isAbsence) {
            Log.d("부재문구 : $callNumberText")
        } else {
            // 숫자일 때 색상 변경 애니메이션 시작
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

    //this.text = getString(R.string.format_four_digit).format(Locale.getDefault(), callNumber)
    private fun ImageView.setTellerImage() {
        val tellerImageFileName = ScreenInfoManager.instance.imgName
        val serverIp = Const.CommunicationInfo.IQS_IP

        Glide.with(requireContext())
            .load("http://$serverIp/image/staff/$tellerImageFileName")
            .fitCenter()
            .transform(RoundedCorners(25))
            .error("${Const.Path.DIR_IMAGE}${Const.Name.DEFAULT_TELLER_IMAGE}")
            .into(this)
    }
}