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
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentMainBinding
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.setPreference

class FragmentMain : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

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
    }


    private fun setUIData() {
        val screenInfo = ScreenInfo.instance

        binding.tvWinNum.text       = screenInfo.winNum.toString()
        binding.tvWinName.text      = screenInfo.winName

        binding.ivTellerImage.setTellerImage()
        binding.tvTellerName.text   = screenInfo.tellerName

        screenInfo.waitNum.observe(viewLifecycleOwner) {
            binding.tvWaitNum.text  = it.toString()
        }

        //기존코드에서는 서비스가 돌고 있지 않으면 통신복구중을 표시하였는데 아직 구현하지 못했음.
        val liveInfoData = MediatorLiveData<String>()
        liveInfoData.addSource(screenInfo.isCrowded) { updateInfoText(liveInfoData) }
        liveInfoData.addSource(screenInfo.systemError) { updateInfoText(liveInfoData) }
        liveInfoData.addSource(screenInfo.tellerMent) { updateInfoText(liveInfoData) }
        liveInfoData.observe(viewLifecycleOwner) { newInfoMessage -> setInfoText(newInfoMessage)}

        val liveCallNumData = MediatorLiveData<String>()
        liveCallNumData.addSource(screenInfo.callNum) { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(screenInfo.systemError) { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(screenInfo.pjt) { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(screenInfo.flagEmpty) { updateCallNumText(liveCallNumData) }
        liveCallNumData.observe(viewLifecycleOwner) { callNumText -> setCallNumberText(callNumText) }
    }

    private fun updateInfoText(liveInfoData: MediatorLiveData<String>) {
        val screenInfo = ScreenInfo.instance
        val newInfoMessage = when {
            screenInfo.systemError.value == 1 -> getString(R.string.msg_system_error)
            screenInfo.isCrowded.value == true-> screenInfo.crowdedMsg
            else -> screenInfo.tellerMent.value
        }
        liveInfoData.value = newInfoMessage
    }

    // liveData 값 업데이트 함수
    private fun updateCallNumText(liveCallNumData: MediatorLiveData<String>) {
        val screenInfo = ScreenInfo.instance
        val emptyMsg = screenInfo.emptyMsg.ifEmpty { getString(R.string.msg_default_absence) }
        val callNumText = when {
            screenInfo.systemError.value == 1 -> getString(R.string.msg_system_error)
            screenInfo.pjt.value == 1         -> getString(R.string.msg_vacancy)
            screenInfo.flagEmpty.value == 1   -> emptyMsg
            else -> "%02d".format(screenInfo.callNum.value)
        }
        liveCallNumData.value = callNumText
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

    private fun setInfoText(text: String) {
        binding.tvInfo.text = text
        val isErrorMessage = text == getString(R.string.msg_network_error) ||
                text == getString(R.string.msg_system_error)
        setMarqueeAnimation(isErrorMessage)
    }
    /**
     * 호출 번호 또는 부재 메시지 설정
     * 1001, 부재중, 공  석 ..등
     */
    private fun setCallNumberText(callNumText: String) {
        val isAbsence = !callNumText.all { it.isDigit() }
        binding.tvCallNum.textSize = if (isAbsence) 330f else 420f
        binding.tvCallNum.text = callNumText

        if (isAbsence) {
            Log.d("부재문구 : $callNumText")
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
        val tellerImageFileName = ScreenInfo.instance.imgName
        val serverIp = Const.CommunicationInfo.IQS_IP

        Glide.with(requireContext())
            .load("http://$serverIp/image/staff/$tellerImageFileName")
            .fitCenter()
            .transform(RoundedCorners(25))
            .error("${Const.Path.DIR_IMAGE}${Const.Name.DEFAULT_TELLER_IMAGE}")
            .into(this)
    }
}