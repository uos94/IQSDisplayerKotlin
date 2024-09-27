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
import com.kct.iqsdisplayer.databinding.FragmentLoadingBinding
import com.kct.iqsdisplayer.ui.FragmentFactory.replaceFragment
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.getCurrentTimeFormatted
import com.kct.iqsdisplayer.util.getLocalIpAddress
import com.kct.iqsdisplayer.util.getMacAddress
import com.kct.iqsdisplayer.util.rebootIQSDisplayer
import kotlinx.coroutines.launch


class FragmentReady : Fragment() {

    private lateinit var binding: FragmentLoadingBinding
    private var mainActivity: MainActivity? = null
    private lateinit var viewModel: SystemReadyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
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

        viewModel = ViewModelProvider(requireActivity())[SystemReadyModel::class.java]

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

        Log.setOnLogEventListener( object : Log.OnLogEventListener {
            override fun onLog(logMessage: String) {
                lifecycleScope.launch {
                    binding.tvLogView.post {
                        binding.tvLogView.append(logMessage + System.lineSeparator())
                        binding.tvLogView.scrollTo(0, binding.tvLogView.bottom)
                    }
                }
            }
        })
        binding.tvLogView.movementMethod = ScrollingMovementMethod()
        binding.tvLogView.text = Log.getLogHistory()
        binding.tvLogView.scrollTo(0, binding.tvLogView.bottom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("onDestroyView")
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