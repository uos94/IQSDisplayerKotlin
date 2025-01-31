package com.kct.iqsdisplayer.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kct.iqsdisplayer.data.packet.receive.BackupCallData
import com.kct.iqsdisplayer.data.packet.receive.CallData
import com.kct.iqsdisplayer.data.packet.receive.LastCallData
import com.kct.iqsdisplayer.data.packet.receive.ReserveData
import com.kct.iqsdisplayer.data.packet.receive.ReserveCallData
import com.kct.iqsdisplayer.data.packet.receive.TellerData
import com.kct.iqsdisplayer.data.packet.receive.WinInfo
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthData
import com.kct.iqsdisplayer.data.packet.receive.MediaListData
import com.kct.iqsdisplayer.data.packet.receive.PausedWorkData
import com.kct.iqsdisplayer.data.packet.receive.ReserveListData
import com.kct.iqsdisplayer.data.packet.receive.toTeller
import com.kct.iqsdisplayer.util.splitData

/**
 * TCP통신으로 받은 데이터를 모든 클래스에서 접근하여 사용하도록 만든 클래스
 * 기존 Java코드의 ScreenInfo와 사용방법이 비슷하고 용도도 같으나 중복코드 불필요코드 모두 삭제하였음.
 */
object ScreenInfo {
    /**혼잡여부(0x0015)패킷에서 windId를 사용한다. windId는 직원정보 설정(0x000D)에서 내려온다.*/
    var winId: Int      = 0
    /** 표시기의 창구 번호 */
    var winNum          = 0
    var listWinInfos    = ArrayList<WinInfo>()

    private val _tellerData = MutableLiveData<TellerData>()
    val tellerData: LiveData<TellerData> = _tellerData
    fun updateTellerData(newTellerData: TellerData) {
        winId = newTellerData.winId
        _tellerData.value = newTellerData
    }

    var playTimeMain    = 10000    //기본값 10초로 설정
    var usePlaySub      = false
    var playTimeRecent  = 10000
    var usePlayMedia    = false
    var playTimeMedia   = 10000
    var playTimeReserveCall   = 20000
    var playTimeReserveList   = 20000
    var mediaFileNameList = ArrayList<String>()
    var volumeLevel     = 1         // 1~10까지의 볼륨값
    var serverTime      = 0
    var isShowWaiting   = false
    var deleteMovieInfo = ""        //패킷에 항목이 있어 받아놓기는 하는데. 사용하는 부분이 없었다.
    var bellFileName    = ""        //호출 시 벨소리 파일명
    var callRepeatCount = 0         //호출 시 반복 출력 횟수
    var callMent        = ""        //호출안내멘트

    private val _isTcpConnected        = MutableLiveData(false) // 공석 여부
    private val _isStopWork            = MutableLiveData(false) // 공석 여부
    private val _isPausedWork          = MutableLiveData(false) // 부재 여부
    private val _isPausedByServerError = MutableLiveData(false) // 부재 ,전산장애

    /** HISON 신규추가, 네트웤 끊김 감지. */
    val isTcpConnected:        LiveData<Boolean> get() = _isTcpConnected        //네트워크 연결 상태
    val isStopWork:            LiveData<Boolean> get() = _isStopWork            //공석여부
    val isPausedWork:          LiveData<Boolean> get() = _isPausedWork          //부재여부, 접속승인때 넘어옴, isPausedByServerError와 따로 넘어옴
    val isPausedByServerError: LiveData<Boolean> get() = _isPausedByServerError //전산장애여부, 접속승인때 넘어옴
    val isCrowded: LiveData<Boolean> get() = _isCrowded

    var pausedWorkMessage = ""
    var crowdedMsg: String = "" // 혼잡 메세지

    val reserveList = ArrayList<ReserveData>() //상담예약리스트
    val lastCallList: MutableLiveData<ArrayList<LastCallData>> = MutableLiveData(ArrayList()) // 최근 호출 리스트

    private val _isCrowded = MutableLiveData(false) // 혼잡여부 BOOL
    fun updateCrowded(isCrowded: Boolean) {
        _isCrowded.postValue(isCrowded)
    }

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

    private val _backupCallInfo = MutableLiveData(BackupCallData())
    val backupCallInfo: LiveData<BackupCallData> get() = _backupCallInfo
    fun updateBackupCall(backupInfo: BackupCallData) {
        _backupCallInfo.postValue(backupInfo)
    }

    private val _normalCallData = MutableLiveData(CallData()) // 일반 Call 번호
    val normalCallData: LiveData<CallData> get() = _normalCallData

    private val _reserveCallInfo = MutableLiveData(ReserveCallData()) // 예약 Call 번호
    val reserveCallInfo: LiveData<ReserveCallData> get() = _reserveCallInfo

    fun updateCallInfo(newCallData: CallData) {
        _normalCallData.postValue(newCallData)
        updateWaitNum(newCallData.winWaitNum)
        lastCallList.postValue(newCallData.lastCallList)
    }

    fun updateReserveCallInfo(newCall: ReserveCallData) {
        _reserveCallInfo.postValue(newCall)
    }

    fun updateDefaultInfo(data: AcceptAuthData) {
        winNum = data.winNum

        updateWinInfos(data.winIds, data.winNames, data.waitNums)

        updateTellerData(data.tellerInfos.toTeller())

        updateWaitNum(listWinInfos.find { it.winId == winId }?.waitNum ?: 0)

        //15000#1#5000#1#10000#woori_travel_15sec.mp4;woori2024.jpg;iqs_backup.jpg;#
        //15000#0#5000#1#10000##
        data.mediaInfos.splitData("#").forEachIndexed { index, value ->
            when (index) {
                0 -> playTimeMain   = value.toInt()
                1 -> usePlaySub     = value == "1"
                2 -> playTimeRecent = value.toInt()
                3 -> usePlayMedia   = value == "1"
                4 -> playTimeMedia  = value.toInt()
                5 -> mediaFileNameList.apply {
                    clear()
                    value.splitData(";").dropWhile { it == "" }.forEach { add(it) }
                }
            }
        }

        volumeLevel = data.volumeLevel.toIntOrNull() ?: 1
        serverTime  = data.serverTime

        data.displaySettingInfo.splitData(";").forEachIndexed { index, value ->
            when (index) {
                0 -> isShowWaiting = value == "1"
                1 -> updateTellerMent(value)
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
                2 -> _isPausedByServerError.postValue(value == "1")
            }
        }

        _isStopWork.postValue(data.stopWork == "1")
    }

    fun updateWinInfos(winIds: String, winNames: String, waits: String) : ArrayList<WinInfo> {
        val splitWinIds      = winIds.splitData(";").asList()
        val splitWinNames    = winNames.splitData(";").asList()
        val splitWaits       = waits.splitData(";").asList()

        listWinInfos.clear()
        listWinInfos.addAll(
            splitWinIds.zip(splitWinNames).zip(splitWaits) { (id, name), wait ->
                WinInfo(id.toInt(), name, wait.toInt())
            })
        return listWinInfos
    }

    //모든 창구의 상담예약리스트가 넘어온다. 나의것만 걸러서 가져오도록 함.
    fun updateReserveList(data: ReserveListData) {
        reserveList.clear()
        val myList = data.reserveList.filter { reserve -> winId == reserve.reserveWinID }
        reserveList.addAll(myList)
    }

    fun addReserveList(data: ReserveData) {
        if(winId == data.reserveWinID) reserveList.add(data)
    }

    fun updateReserveList(data: ReserveData) {
        reserveList.replaceAll{ if(it.reserveNum == data.reserveNum) data else it }
    }

    fun cancelReserve(data: ReserveData) {
        reserveList.removeIf { it.reserveNum == data.reserveNum }
    }

    fun arriveReserve(data: ReserveData) {
        reserveList.replaceAll{ if(it.reserveNum == data.reserveNum) data else it }
    }

    fun updateMediaList(data: MediaListData) {
        mediaFileNameList.clear()
        mediaFileNameList.addAll(data.mediaList)
    }

    fun updatePausedWork(data: PausedWorkData) {
        _isPausedWork.postValue(data.isPausedWork)
        pausedWorkMessage   = data.pausedMessage
    }

    fun getWinName(winId: Int): String {
        val winName = listWinInfos.find { it.winId == winId }?.winName ?: ""
        return winName
    }

    fun setSocketConnected(isConnected: Boolean) {
        _isTcpConnected.postValue(isConnected)
    }

    /** 디버깅을 위해 모든 정보를 출력하였음.*/
    override fun toString(): String {
        return """
================================================================================
    ScreenInfo 정보확인              
================================================================================
winId       =$winId
winNum      =$winNum
창구정보      =$listWinInfos
직원정보      =$tellerData
사용여부[서브모니터]=$usePlaySub
사용여부[영상] =$usePlayMedia
표출시간[메인화면]=$playTimeMain
표출시간[영상] =$playTimeMedia
표출시간[최근호출리스트]=$playTimeRecent
표출시간[예약호출]=$playTimeReserveCall
표출시간[예약리스트]=$playTimeReserveList
영상재생목록  =$mediaFileNameList
volumeLevel =$volumeLevel
serverTime  =$serverTime
대기인수화면표시여부=$isShowWaiting
bellFileName ='$bellFileName'
호출반복횟수  =$callRepeatCount
호출안내멘트  ='$callMent'
네트워크연결여부=${_isTcpConnected.value}
공석여부     =${_isStopWork.value}
업무일시정지  =${_isPausedWork.value}
전산장애여부  =${_isPausedByServerError.value}
혼잡여부     =${_isCrowded.value}
혼잡메세지   ='$crowdedMsg'
부재중메세지  ='$pausedWorkMessage'
예약리스트   =$reserveList
최근호출리스트=$lastCallList
tellerMent  =${_tellerMent.value}
대기인수     =${_waitNum.value}
백업호출정보  =${_backupCallInfo.value}
호출정보     =${_normalCallData.value}
예약호출정보  =${_reserveCallInfo.value}
================================================================================
        """.trimIndent()
    }
}