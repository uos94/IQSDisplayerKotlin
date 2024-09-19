package com.kct.iqsdisplayer.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kct.iqsdisplayer.data.Call
import com.kct.iqsdisplayer.data.LastCall
import com.kct.iqsdisplayer.data.Reserve
import com.kct.iqsdisplayer.data.Teller
import com.kct.iqsdisplayer.data.WinInfo
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.InfoMessageRequest
import com.kct.iqsdisplayer.data.packet.receive.MediaListResponse
import com.kct.iqsdisplayer.data.packet.receive.PausedWorkRequest
import com.kct.iqsdisplayer.data.packet.receive.ReserveListResponse
import com.kct.iqsdisplayer.data.toTeller
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.splitData

/** 실시간으로 필요한 변수만 LiveData로 한다.*/
class SharedViewModel : ViewModel() {
    //혼잡여부(0x0015)패킷에서만 windId를 사용한다. windId는 직원정보 설정(0x000D)에서 내려온다.
    var winId: Int = 0

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
    var workingMessage = ""
    var deleteMovieInfo = ""                //사용 안하는 것으로 예상된다.
    var bellFileName = ""
    var callRepeatCount = 0
    var callMent = ""                       //호출안내멘트
    private val _isPausedWork = MutableLiveData(false) // 부재 여부
    val isPausedWork: LiveData<Boolean> get() = _isPausedWork //부재여부, 접속승인때 넘어옴, isPausedByServerError와 따로 넘어옴

    var pausedWorkMessage = ""
    var isPausedByServerError = false       //전상장애여부, 접속승인때 넘어옴
    var isStopWork = false                  //구 PJT, 공석여부

    val reserveList = ArrayList<Reserve>() //상담예약리스트
    val lastCallList = ArrayList<LastCall>() //상담예약리스트

    /** 전산 장애 설정, 0 정상운영 1: 전산장애 */
    private val _systemError = MutableLiveData(0) // 창구 대기 인원
    val systemError: LiveData<Int> get() = _systemError
    fun updateSystemError(systemError: Int) {
        _systemError.postValue(systemError)
    }

    private val _isCrowded = MutableLiveData(false) // 혼잡여부 BOOL
    val isCrowded: LiveData<Boolean> get() = _isCrowded
    fun updateCrowded(isCrowded: Boolean) {
        _isCrowded.postValue(isCrowded)
    }
    var crowdedMsg: String = "" // 혼잡 메세지

    private val _tellerMent = MutableLiveData("") // 하단 안내 문구
    val tellerMent: LiveData<String> get() = _tellerMent
    fun updateTellerMent(tellerMent: String) {
        _tellerMent.postValue(tellerMent)
    }

    private val _waitNum = MutableLiveData(0) // 창구 대기 인원
    val waitNum: LiveData<Int> get() = _waitNum
    fun updateWaitNum(newWaitNum: Int) {
        _waitNum.postValue(newWaitNum)
    }

    private val _callInfo = MutableLiveData(Call()) // 호출패킷 저장
    val callInfo: LiveData<Call> get() = _callInfo

    fun updateCallInfo(newCall: Call) {
        _callInfo.postValue(newCall)
        updateWaitNum(newCall.winWaitNum)
    }

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

        updateWinInfos(data.winIdList, data.winNameList, data.waitingNumList)

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
                1 -> workingMessage = value
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
                0 -> _isPausedWork.postValue(value == "1")
                1 -> pausedWorkMessage      = value
                2 -> isPausedByServerError  = value == "1"
            }
        }

        isStopWork = data.stopWork == "1"
    }

    fun updateWinInfos(winIds: String, winNames: String, waits: String) {
        val splitWinIds      = winIds.splitData(";").asList()
        val splitWinNames    = winNames.splitData(";").asList()
        val splitWaits       = waits.splitData(";").asList()

        listWinInfos.clear()
        listWinInfos.addAll(
            splitWinIds.zip(splitWinNames).zip(splitWaits) { (id, name), wait ->
                WinInfo(id.toInt(), name, wait.toInt())
            })
    }

    //모든 창구의 상담예약리스트가 넘어온다. 나의것만 걸러서 가져오도록 함.
    fun updateReserveList(data: ReserveListResponse) {
        reserveList.clear()
        val myList = data.reserveList.filter { reserve -> winNum == reserve.reserveWinID }
        reserveList.addAll(myList)
    }

    fun addReserveList(data: Reserve) {
        if(winId == data.reserveWinID) reserveList.add(data)
    }

    fun updateReserveList(data: Reserve) {
        reserveList.replaceAll{ if(it.reserveNum == data.reserveNum) data else it }
    }

    fun cancelReserve(data: Reserve) {
        reserveList.removeIf { it.reserveNum == data.reserveNum }
    }

    fun arriveReserve(data: Reserve) {
        reserveList.replaceAll{ if(it.reserveNum == data.reserveNum) data else it }
    }

    fun updateMediaList(data: MediaListResponse) {
        Log.d("영상재생 리스트 업데이트 : ${data.mediaList}")
        mediaFileNameList.clear()
        mediaFileNameList.addAll(data.mediaList)
    }

    fun updatePausedWork(data: PausedWorkRequest) {
        Log.d("부재상태 업데이트 : $data")
        _isPausedWork.postValue(data.isPausedWork)
        pausedWorkMessage   = data.pausedMessage
    }

    fun updateInfoMessage(data: InfoMessageRequest) {
        workingMessage = data.infoMessage
    }

}