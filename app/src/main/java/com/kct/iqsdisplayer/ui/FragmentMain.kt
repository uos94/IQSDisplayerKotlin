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
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentMainBinding
import com.kct.iqsdisplayer.util.Log

class FragmentMain : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var mainActivity: MainActivity? = null

    private val marquee by lazy { AnimationUtils.loadAnimation(activity, R.anim.marquee).apply { interpolator = LinearInterpolator() } }

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
        binding.tvDeskNum.text       = getString(R.string.format_two_digit).format(ScreenInfo.winNum)
        binding.tvDeskNum.setOnLongClickListener {
            FragmentFactory.replaceFragment(FragmentFactory.Index.FRAGMENT_READY)
            true
        }
        binding.tvDeskName.text      = ScreenInfo.getWinName(ScreenInfo.winId)
        binding.tvCallNum.text       = updateCallNumText(null)

        binding.ivTellerImg.setTellerImage()
        binding.tvTellerName.text   = ScreenInfo.tellerInfo.tellerName

        ScreenInfo.waitNum.observe(viewLifecycleOwner) {
            binding.tvWaitingNum.text  = it.toString()
        }

        val liveInfoData = MediatorLiveData<String>()
        liveInfoData.addSource(ScreenInfo.isTcpConnected)        { updateInfoText(liveInfoData) }
        liveInfoData.addSource(ScreenInfo.isPausedByServerError) { updateInfoText(liveInfoData) }
        liveInfoData.addSource(ScreenInfo.isCrowded)             { updateInfoText(liveInfoData) }
        liveInfoData.addSource(ScreenInfo.tellerMent)            { updateInfoText(liveInfoData) }
        liveInfoData.observe(viewLifecycleOwner) { newInfoMessage -> setInfoText(newInfoMessage)}

        val liveCallNumData = MediatorLiveData<String>()
        liveCallNumData.addSource(ScreenInfo.normalCallInfo)        { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(ScreenInfo.isPausedWork)          { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(ScreenInfo.isStopWork)            { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(ScreenInfo.isTcpConnected)        { updateCallNumText(liveCallNumData) }
        liveCallNumData.addSource(ScreenInfo.isPausedByServerError) { updateCallNumText(liveCallNumData) }
        liveCallNumData.observe(viewLifecycleOwner) { callNumText -> setCallNumberText(callNumText) }
    }

    //우선순위 1.부재중 2.창구혼잡메세지 3.TellerMent, 부재메세지는 CallNum에 크게 띄움.
    private fun updateInfoText(liveInfoData: MediatorLiveData<String>) {
        //Log.d("updateInfoText isTcpConnected:${ScreenInfo.isTcpConnected.value} isPausedByServerError:${ScreenInfo.isPausedByServerError.value} isCrowded:${ScreenInfo.isCrowded.value}")
        val newInfoMessage = when {
            ScreenInfo.isTcpConnected.value == false       -> getString(R.string.msg_system_error)
            ScreenInfo.isPausedByServerError.value == true -> getString(R.string.msg_system_error)
            ScreenInfo.isCrowded.value == true             -> ScreenInfo.crowdedMsg
            else -> ScreenInfo.tellerMent.value
        }
        liveInfoData.value = newInfoMessage
    }

    //우선순위 1.공석, 2.부재중, 3.호출번호
    private fun updateCallNumText(liveCallNumData: MediatorLiveData<String>?) : String {

        val emptyMsg = ScreenInfo.tellerInfo.emptyMsg.ifEmpty { getString(R.string.msg_default_absence) }
        val callNumText = when {
            ScreenInfo.isStopWork.value == true            -> getString(R.string.msg_vacancy)
            ScreenInfo.isTcpConnected.value == false       -> getString(R.string.msg_system_error)
            ScreenInfo.isPausedByServerError.value == true -> getString(R.string.msg_system_error)
            ScreenInfo.isPausedWork.value == true          -> emptyMsg
            else -> getString(R.string.format_four_digit).format(ScreenInfo.normalCallInfo.value?.callNum)
        }
        liveCallNumData?.value = callNumText
        return callNumText
    }

    private fun setMarqueeAnimation(isErrorMessage: Boolean) {
        //Log.d("setMarqueeAnimation $isErrorMessage, text[${binding.tvInfo.text}]")
        if (isErrorMessage) {
            binding.tvInfo.clearAnimation()
            binding.tvInfo.gravity = Gravity.CENTER
        } else {

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
     * TODO : autoSize 적용 해야함. 하지만 api 26이상 부터 가능.
     */
    private fun setCallNumberText(callNumText: String) {
        val callNumTextSize = when {
            ScreenInfo.isStopWork.value == true -> 250f
            ScreenInfo.isTcpConnected.value == false -> 200f
            ScreenInfo.isPausedByServerError.value == true -> 200f
            ScreenInfo.isPausedWork.value == true -> 250f
            else -> 370f
        }
        val isAbsence = !callNumText.all { it.isDigit() }
        binding.tvCallNum.textSize = callNumTextSize

        val oldText = binding.tvCallNum.text
        binding.tvCallNum.text = callNumText

        if (!isAbsence && oldText != callNumText) {
            // 숫자일 때 색상 변경 애니메이션 시작
            val colorAnim = ObjectAnimator.ofInt(
                binding.tvCallNum, // 애니메이션을 적용할 TextView 객체
                "textColor", // 변경할 속성 이름
                Color.WHITE,
                Color.BLACK
            ).apply {
                duration = 1000 // 1초 동안 색상 변경
                setEvaluator(ArgbEvaluator())
                repeatCount = 5 // 총 6번 변경 (시작 시 검은색, 5번 반복)
                repeatMode = ValueAnimator.REVERSE
            }
            // 애니메이션 종료 시 텍스트 색상을 검은색으로 고정
            colorAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.tvCallNum.setTextColor(Color.WHITE)
                }
            })
            colorAnim.start()
        }
    }

    private fun ImageView.setTellerImage() {
        val tellerImageFileName = ScreenInfo.tellerInfo.tellerImg

        Glide.with(requireContext())
            .load("${Const.Path.DIR_TELLER_IMAGE}$tellerImageFileName")
            .signature(ObjectKey(System.currentTimeMillis().toString()))
            .fitCenter()
            .error("${Const.Path.DIR_IMAGE}${Const.Name.DEFAULT_TELLER_IMAGE}")
            .into(this)
    }
}