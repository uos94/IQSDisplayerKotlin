package com.kct.iqsdisplayer.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.BuildConfig
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.Const.CommunicationInfo.loadCommunicationInfo
import com.kct.iqsdisplayer.databinding.FragmentInitBinding
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.copyFile
import com.kct.iqsdisplayer.util.getLocalIpAddress
import com.kct.iqsdisplayer.util.getMacAddress
import java.io.File

class FragmentInit : Fragment() {

    private var _binding: FragmentInitBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //설정파일 복구
        restoreSharedPreferencesFiles()

        //SharedPreferences 값 CommunicationInfo 에 저장
        initCommunicationInfo()

        binding.tvVersionInfo.text = BuildConfig.VERSION_NAME
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** SharedPreference 파일이 없을 경우 백업본 카피 복구 */
    private fun restoreSharedPreferencesFiles() {
        var prefFileName = Const.Name.getPrefDisplayerSettingName()

        val prefDisplayerSetting = File(Const.Path.Device.DIR_SHARED_PREFS, prefFileName)
        if (prefDisplayerSetting.exists()){
            Log.d("설정정보파일 정상[${Const.Name.PREF_DISPLAYER_SETTING}]" )
        }
        else {
            val fileName = Const.Name.getPrefDisplayerSettingName()
            val sourcePath = "${Const.Path.Device.DIR_IQS}$fileName"
            val destPath = "${Const.Path.Device.DIR_SHARED_PREFS}$fileName"
            context?.let { copyFile(it, sourcePath, destPath) }
        }

        prefFileName = Const.Name.getPrefDisplayInfoName()

        val prefDisplayInfo = File(Const.Path.Device.DIR_SHARED_PREFS, prefFileName)
        if (prefDisplayInfo.exists()){
            Log.d("화면정보파일 정상[${Const.Name.PREF_DISPLAY_INFO}]" )
        }
        else {
            val fileName = Const.Name.getPrefDisplayInfoName()
            val sourcePath = "${Const.Path.Device.DIR_IQS}$fileName"
            val destPath = "${Const.Path.Device.DIR_SHARED_PREFS}$fileName"
            context?.let { copyFile(it, sourcePath, destPath) }
        }
    }

    /** 설정 파일 값 저장 */
    private fun initCommunicationInfo() {
        //설정파일 값 저장
        context?.let { context ->
            val pref = context.getSharedPreferences(Const.Name.PREF_DISPLAYER_SETTING, Context.MODE_PRIVATE)
            pref.loadCommunicationInfo()
        }

        Const.CommunicationInfo.MY_IP = getLocalIpAddress()
        Const.CommunicationInfo.MY_MAC = getMacAddress()
    }






}