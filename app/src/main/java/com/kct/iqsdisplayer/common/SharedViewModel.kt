package com.kct.iqsdisplayer.common

import androidx.lifecycle.ViewModel
import com.kct.iqsdisplayer.data.Reserve
import com.kct.iqsdisplayer.data.Teller
import com.kct.iqsdisplayer.data.WinInfo
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.MediaListResponse
import com.kct.iqsdisplayer.data.packet.receive.ReserveListResponse
import com.kct.iqsdisplayer.data.toTeller
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.splitData

/** 실시간으로 필요한 변수만 LiveData로 한다.*/
class SharedViewModel : ViewModel() {
    /** 표시기의 창구 번호 */
    var winNum = 0
    var listWinInfos = ArrayList<WinInfo>()
    var tellerInfo: Teller? = null
    var playTimeMain = 0
    var usePlaySub = false
    var playTimeSub = 0
    var usePlayMedia = false
    var playTimeMedia = 0
    var mediaFileNameList = ArrayList<String>()
    var volumeLevel = 1
    var serverTime = 0
    var isShowWaiting = false
    var rotateMessage = ""
    var deleteMovieInfo = "" //사용 안하는 것으로 예상된다.
    var bellFileName = ""
    var callRepeatCount = 0
    var callMent = "" //호출안내멘트
    var isPausedWork = false
    var pausedWorkMessage = ""
    var isPausedByServerError = false
    var isNotWork = false   //구 PJT

    val reserveList = ArrayList<Reserve>() //상담예약리스트
/*    private val _sharedData = MutableLiveData<String>()
    val sharedData: LiveData<String> = _sharedData

    fun updateSharedData(newData: String) {
        _sharedData.value = newData
    }
    
    private val _winWaitMap = MutableLiveData<HashMap<Int, WinInfo>>()
    val winWaitMap: LiveData<HashMap<Int, WinInfo>> = _winWaitMap

    init {
        _winWaitMap.value = HashMap<Int, WinInfo>()
    }

    fun updateWinInfo(winWait: WinInfo) {
        val updatedMap = _winWaitMap.value?.clone() as HashMap<Int, WinInfo>
        updatedMap[winWait.winID] = winWait
        _winWaitMap.value = updatedMap
    }*/

    /** AcceptAuthResponse를 받았을 때 기본적인 정보는 거의 다 내려온다.
     * 데이터가 많아서 여기서 한번 더 가공한다.*/
    fun updateDefaultInfo(data: AcceptAuthResponse) {
        winNum = data.winNum

        val winIds      = data.winNumList.splitData(";")
        val winNames    = data.winNameList.splitData(";")
        val waits       = data.waitingNumList.splitData(";")

        listWinInfos.clear()
        listWinInfos.addAll(
            winIds.zip(winNames).zip(waits) { (id, name), wait ->
            WinInfo(id.toInt(), name, wait.toInt())
        })

        tellerInfo = data.tellerInfo.toTeller()

        //15000#1#5000#1#10000#woori_travel_15sec.mp4;woori2024.jpg;iqs_backup.jpg;#
        data.mediaInfo.splitData("#").forEachIndexed { index, value ->
            when (index) {
                0 -> playTimeMain   = value.toInt()
                1 -> usePlaySub     = value == "1"
                2 -> playTimeSub    = value.toInt()
                3 -> usePlayMedia   = value == "1"
                4 -> playTimeMedia  = value.toInt()
                5 -> mediaFileNameList.apply { clear(); addAll(value.splitData(";")) }
            }
        }

        volumeLevel = data.volumeLevel.toIntOrNull() ?: 1
        serverTime  = data.serverTime

        data.displaySettingInfo.splitData(";").forEachIndexed { index, value ->
            when (index) {
                0 -> isShowWaiting = value == "1"
                1 -> rotateMessage = value
            }
        }

        deleteMovieInfo = data.deleteMovieInfo
        bellFileName    = data.bellFileName
        callRepeatCount = data.callRepeatCount.toIntOrNull() ?: 1
        callMent        = when(data.callMentNum) {
            "1" -> "창구로 모시겠습니다."
            "2" -> "창구로 도와드리겠습니다."
            else -> "창구로 오십시오."
        }

        data.pausedWork.splitData(";").forEachIndexed { index, value ->
            when (index) {
                0 -> isPausedWork           = value == "1"
                1 -> pausedWorkMessage      = value
                2 -> isPausedByServerError  = value == "1"
            }
        }

        isNotWork = data.notWork == "1"
    }

    //TODO : 모든 창구의 상담예약리스트가 넘어오는건지 확인을 해야한다.
    fun updateReserveList(data: ReserveListResponse) {
        reserveList.clear()
        val myList = data.reserveList.filter { reserve -> winNum == reserve.reserveWinID }
        reserveList.addAll(myList)
    }
    fun updateMediaList(data: MediaListResponse) {
        Log.d("영상재생 리스트 업데이트 : ${data.mediaList}")
        mediaFileNameList.clear()
        mediaFileNameList.addAll(data.mediaList)
    }
}