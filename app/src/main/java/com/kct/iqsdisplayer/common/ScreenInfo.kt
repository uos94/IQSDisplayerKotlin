package com.kct.iqsdisplayer.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kct.iqsdisplayer.data.packet.receive.BackupCallInfo
import com.kct.iqsdisplayer.data.packet.receive.Call
import com.kct.iqsdisplayer.data.packet.receive.LastCall
import com.kct.iqsdisplayer.data.packet.receive.Reserve
import com.kct.iqsdisplayer.data.packet.receive.ReserveCall
import com.kct.iqsdisplayer.data.packet.receive.Teller
import com.kct.iqsdisplayer.data.packet.receive.WinInfo
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthResponse
import com.kct.iqsdisplayer.data.packet.receive.MediaListResponse
import com.kct.iqsdisplayer.data.packet.receive.PausedWorkRequest
import com.kct.iqsdisplayer.data.packet.receive.ReserveListResponse
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
    var tellerInfo: Teller = Teller()
        set(value) {
            field = value
            winId = value.winId
        }
    var playTimeMain    = 10000    //기본값 10초로 설정
    var usePlaySub      = false
    var playTimeRecent  = 10000
    var usePlayMedia    = false
    var playTimeMedia   = 10000
    var playTimeReserveCall   = 20000   //서버에서 내려주는 값없음. 기본값
    var playTimeReserveList   = 20000   //서버에서 내려주는 값없음. 기본값
    var mediaFileNameList = ArrayList<String>()
    var volumeLevel     = 1         // 1~10까지의 볼륨값
    var serverTime      = 0
    var isShowWaiting   = false
    var deleteMovieInfo = ""        //사용 안하는 것으로 예상된다.
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

    val reserveList = ArrayList<Reserve>() //상담예약리스트
    val lastCallList: MutableLiveData<ArrayList<LastCall>> = MutableLiveData(ArrayList()) // 최근 호출 리스트

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

    private val _backupCallInfo = MutableLiveData(BackupCallInfo())
    val backupCallInfo: LiveData<BackupCallInfo> get() = _backupCallInfo
    fun updateBackupCall(backupInfo: BackupCallInfo) {
        _backupCallInfo.postValue(backupInfo)
    }

    private val _normalCallInfo = MutableLiveData(Call()) // 호출패킷 저장
    val normalCallInfo: LiveData<Call> get() = _normalCallInfo

    private val _reserveCallInfo = MutableLiveData(ReserveCall()) // 호출패킷 저장
    val reserveCallInfo: LiveData<ReserveCall> get() = _reserveCallInfo

    fun updateCallInfo(newCall: Call) {
        _normalCallInfo.postValue(newCall)
        updateWaitNum(newCall.winWaitNum)
        lastCallList.postValue(newCall.lastCallList)
    }

    fun updateReserveCallInfo(newCall: ReserveCall) {
        _reserveCallInfo.postValue(newCall)
    }

    /** AcceptAuthResponse를 받았을 때 기본적인 정보는 거의 다 내려온다.
     * 데이터가 많아서 여기서 한번 더 가공한다.*/
    fun updateDefaultInfo(data: AcceptAuthResponse) {
        winNum = data.winNum

        updateWinInfos(data.winIdList, data.winNameList, data.waitingNumList)

        tellerInfo = data.tellerInfo.toTeller()

        winId = tellerInfo.winId

        updateWaitNum(listWinInfos.find { it.winID == winId }?.waitNum ?: 0)

        //15000#1#5000#1#10000#woori_travel_15sec.mp4;woori2024.jpg;iqs_backup.jpg;#
        data.mediaInfo.splitData("#").forEachIndexed { index, value ->
            when (index) {
                0 -> playTimeMain   = value.toInt()
                1 -> usePlaySub     = value == "1"
                2 -> playTimeRecent = value.toInt()
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
        mediaFileNameList.clear()
        mediaFileNameList.addAll(data.mediaList)
    }

    fun updatePausedWork(data: PausedWorkRequest) {
        _isPausedWork.postValue(data.isPausedWork)
        pausedWorkMessage   = data.pausedMessage
    }

    fun getWinName(winId: Int): String {
        val winName = listWinInfos.find { it.winID == winId }?.winName ?: ""
        return winName
    }

    fun setSocketConnected(isConnected: Boolean) {
        _isTcpConnected.postValue(isConnected)
    }



}