package com.kct.iqsdisplayer.ui

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.SystemReadyModel
import com.kct.iqsdisplayer.databinding.FragmentReadyBinding
import com.kct.iqsdisplayer.ui.FragmentFactory.Index
import com.kct.iqsdisplayer.ui.FragmentFactory.replaceFragment
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.getCurrentTimeFormatted
import com.kct.iqsdisplayer.util.getLocalIpAddress
import com.kct.iqsdisplayer.util.getMacAddress
import com.kct.iqsdisplayer.util.rebootIQSDisplayer
import kotlinx.coroutines.launch


class FragmentReady : Fragment() {

    private lateinit var binding: FragmentReadyBinding
    private lateinit var vmSystemReady: SystemReadyModel
    private var mainActivity: MainActivity? = null

    private var userTouchedScrollView = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vmSystemReady = ViewModelProvider(requireActivity())[SystemReadyModel::class.java]

        setUI()
    }

    private fun setUI() {
        binding.btSetting.setOnClickListener(clickListener)
        binding.btReboot.setOnClickListener(clickListener)
        binding.btShutdown.setOnClickListener(clickListener)
        binding.btMoveMain.setOnClickListener(clickListener)
        binding.tvAppVersion.text = "App Version ${BuildConfig.VERSION_NAME}"

        binding.clSystemDate.tvKey.text    = "현재시간"
        binding.clSystemDate.tvValue.text  = getCurrentTimeFormatted()

        binding.clMacAddress.tvKey.text    = "MAC주소"
        binding.clMacAddress.tvValue.text  = getMacAddress()

        binding.clNetworkInfo.tvKey.text   = "IP"
        binding.clNetworkInfo.tvValue.text = getLocalIpAddress()

        setLogText(Log.getLogHistory())

        Log.setOnLogEventListener( object : Log.OnLogEventListener {
            override fun onLog(logMessage: String) {
                setLogText(logMessage)
            }
        })
        binding.svLogLayer.viewTreeObserver.addOnScrollChangedListener {
            val scrollViewHeight = binding.svLogLayer.height
            val scrollContentHeight = binding.svLogLayer.getChildAt(0).height
            val scrollY = binding.svLogLayer.scrollY

            // 스크롤 뷰 최하단에서부터의 거리 계산
            val distanceFromBottom = scrollContentHeight - (scrollY + scrollViewHeight)

            // 예: 최하단 50픽셀 이내에 있는 경우 자동 스크롤
            val bottomThreshold = 50

            userTouchedScrollView = distanceFromBottom > bottomThreshold
        }
    }

    private fun setLogText(logMessage: String) {
        lifecycleScope.launch {
            binding.tvLogView.append(logMessage + System.lineSeparator())

            if(!userTouchedScrollView) {
                binding.svLogLayer.post {
                    binding.svLogLayer.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.setOnLogEventListener(null)
    }

    private val clickListener = View.OnClickListener {
        when (it.id) {
            R.id.btSetting -> {
                Log.i("설정버튼 선택")
                replaceFragment(FragmentFactory.Index.FRAGMENT_SETTING)
            }
            R.id.btReboot -> {
                Log.i("재부팅버튼 선택")
                rebootIQSDisplayer()
            }
            R.id.btShutdown -> {
                Log.i("프로그램종료버튼 선택")
                mainActivity?.finishApp("프로그램종료버튼 선택")
            }
            R.id.btMoveMain -> {
                Log.i("메인화면이동 선택")
                replaceFragment(FragmentFactory.Index.FRAGMENT_MAIN)
            }
            else -> {
                Log.e("알 수 없는 버튼")
            }
        }
    }



}
