package com.kct.iqsdisplayer.ui

import SettingListAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.Const.CallViewMode
import com.kct.iqsdisplayer.data.SettingItem
import com.kct.iqsdisplayer.databinding.FragmentSettingBinding
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.getPreference
import com.kct.iqsdisplayer.util.setPreference

class FragmentSetting : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity?.onConnectRetry()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUIData()

        mainActivity?.stopTcpClient()
    }

    private fun setUIData() {
        binding.lvSetting.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SettingListAdapter().apply {setOnItemClickListener(listItemClickListener) }
        }

    }

    private val listItemClickListener = object : SettingListAdapter.OnItemClickListener {
        override fun onItemClick(position: Int, item: SettingItem) {
            // 대화상자 생성
            val builder = AlertDialog.Builder(requireContext())
            val input = EditText(requireContext())
            val pref = if(item.mainText == "표시기IP") Const.Name.PREF_DISPLAY_INFO else Const.Name.PREF_DISPLAYER_SETTING

            val loadSettingPrefData = requireContext().getPreference(pref, item.prefKey, item.prefDefaultValue)

            Log.d("Pref에서 읽어오기 : name[$pref, key[${item.prefKey}], value[$loadSettingPrefData]")

            input.setText(loadSettingPrefData)
            builder.setView(input)

            builder.setTitle(item.mainText)
            builder.setMessage(R.string.msg_alert_setting)

            builder.setPositiveButton(R.string.ok) { _, _ ->
                val newValue = input.text.toString()

                requireContext().setPreference(pref, item.prefKey, newValue)
                Log.d("Pref에 저장 : name[${pref}, key[${item.prefKey}], value[$newValue]")
                when (item.mainText) {
                    "표시기IP"        -> Const.ConnectionInfo.DISPLAY_IP = newValue
//                    "사운드파일위치"   -> Const.Path.DIR_SOUND = newValue //변경불가
//                    "홍보영상파일위치"  -> Const.Path.DIR_VIDEO = newValue //변경불가
//                    "이미지파일위치"    -> Const.Path.DIR_IMAGE = newValue //변경불가
                    "발행기IP"        -> Const.ConnectionInfo.IQS_IP = newValue
                    "발행기PORT"      -> Const.ConnectionInfo.IQS_PORT = newValue.toIntOrNull() ?: 8697
                    "백업서버IP"      -> { /* 사용 안 함 */ }
                    "백업서버PORT"    -> { /* 사용 안 함 */ }
                    "호출화면"        -> {
                        Const.ConnectionInfo.CALLVIEW_MODE = when (newValue) {
                            "0" -> CallViewMode.MAIN
                            "2" -> CallViewMode.SUB
                            "3" -> CallViewMode.SOUND
                            else -> CallViewMode.MAIN
                        }
                        Toast.makeText(requireContext(), "호출화면을 변경은 재시작 이후 적용됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                Log.i("${item.mainText} : $newValue 저장")
            }

            builder.setNegativeButton(R.string.cancel) { _, _ -> Log.i("취소 선택") }

            val alert = builder.create()
            alert.show()
        }
    }
}