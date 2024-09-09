package com.kct.iqsdisplayer.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kct.iqsdisplayer.data.LastCall
import com.kct.iqsdisplayer.data.Reserve
import com.kct.iqsdisplayer.data.Sound
import com.kct.iqsdisplayer.data.Teller
import com.kct.iqsdisplayer.data.TestVolume
import com.kct.iqsdisplayer.data.WinWait
import com.kct.iqsdisplayer.data.packet.receive.AcceptAuthResponseData
import com.kct.iqsdisplayer.data.packet.receive.CallCancelData
import com.kct.iqsdisplayer.data.packet.receive.CallRequestData
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.removeChar
import com.kct.iqsdisplayer.util.splitData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenInfo private constructor() {
    companion object {
        @JvmField
        val instance: ScreenInfo = ScreenInfo()
    }

    //기본변수들 모두 teller에 포함되어 있음.
    var tellerID: Int = 0 // 직원 ID
    var winID: Int = 0 // 소속 창구 ID
    var job: String = "" // 직무명
    var imgName: String = "" // 직원사진 파일명
    var pcIP: String = "" // 직원 PC IP
    var bkDisplay: Int = 0 // 백업 표시기 번호
    var bkWay: Int = 1 // 백업 표시기 방향
    var profile1: String = "" // 직원 프로필1
    var profile2: String = "" // 직원 프로필2
    var tellerNum: Int = 0 // 직원 행번
    var displayIP: String = "" // 직원 표시기 IP
    var tellerName: String = "" // 직원명

    private val _pjt = MutableLiveData(1) // 공석 설정    (True 일 경우 공석 화면 표시)
    val pjt: LiveData<Int> get() = _pjt
    fun updatePjt(pjt: Int) {
        _pjt.postValue(pjt)
    }

    //var teller:Teller? = null
    /** 창구명 */
    var winName: String = ""       // 소속 창구명
    /** 창구 번호 , 기본값 0 */
    var winNum: Int = 0            // 창구 번호
    //var winNumIndex: Int = 0 // 230531, by HAHU  창구 인덱스 저장, HISON del : 안쓰고 있음
    //var winNumIndexList: String = "" // 230531, by HAHU  창구 인덱스 저장, HISON del : 안쓰고 있음
    //var displayType: String = "" // 표시기 화면 타입, HISON del : CommunicationInfo의 CallView와 중복됨
    //var displayColor: String = "" // 표시기 색상, HISON del : 안쓰고 있음
    //var winDesc: String = "" // 창구 설명, HISON del : 안쓰고 있음
    var emptyMsg: String = ""      // 부재 메세지

    private val _flagEmpty = MutableLiveData(0) // 부재 여부
    val flagEmpty: LiveData<Int> get() = _flagEmpty
    fun updateFlagEmpty(flagEmpty: Int) {
        _flagEmpty.postValue(flagEmpty)
    }

    /** 대기인수 표시 유무 T/F, 기본값 0 */
    var availableWaitInfo: Int = 0 //
    /** 화면테마 0 검정 1 파랑, 기본값 0 */
    var theme: Int = 0 //
    /** 안내멘트 기본값 "" */
    var ment: String = "" //

    var bellInfo: String = "" // 호출 시 벨소리 파일명
    var callInfo: String = "1" // 호출 시 반복 출력 횟수
    var volumeInfo: String = "1" // 1~10까지의 볼륨값


    // public static String MediaMode;      // 미디어 정보에서 얻은 모드 상태

    /** 전산 장애 설정, 0 정상운영 1: 전산장애 */
    private val _systemError = MutableLiveData(0) // 창구 대기 인원
    val systemError: LiveData<Int> get() = _systemError
    fun updateSystemError(systemError: Int) {
        _systemError.postValue(systemError)
    }

    var winList: ArrayList<WinWait> = ArrayList() // 창구별 ID 창구명 대기인원 리스트
    var tellerList: ArrayList<Teller> = ArrayList() // 직원 정보 리스트

    var ticketNum: Int = 0 // 발권 번호

    private val _waitNum = MutableLiveData(0) // 창구 대기 인원
    val waitNum: LiveData<Int> get() = _waitNum
    fun updateWaitNum(newWaitNum: Int) {
        _waitNum.postValue(newWaitNum)
    }

    var display: Int = 0 // 직원 정보 리스트에서 받아오는 표시

    var errorStatus: Int = 0 // 장애 여부

    private val _callNum = MutableLiveData(0) // 창구 대기 인원
    val callNum: LiveData<Int> get() = _callNum

    fun updateCallNum(newCallNum: Int) {
        _callNum.postValue(newCallNum)
    }

    var ticketWinID: Int = 0 // 발권 창구 ID
    var callWinID: Int = 0 // 호출 창구 ID

    // public static int winWaitNum;           // 대기 인수
    var callBkDisplay: Int = 0 // 백업 표시기 번호
    var callBkWay: Int = 0 // 화살표 방향
    var reserve: Int = 0 // 예약 구분 0:일반고객, 1:예약발권 예약 고객, 2:즉시발권 예약고객
    /** 일반인[0], VIP[1] */
    var flagVIP: Int = 0 // VIP실 구분 -  [2023.05.17][add kimhj]

    /** 호출 횟수 설정 */
    var collectNum: Int = 0 // 호출 횟수

    var cancelError: Int = 0 // 장애 여부
    var cancelcallNum: Int = 0 // 호출 번호

    // public static int Wait;                 // 대기인수
    var callWinNum: Int = 0 // 호출 창구 번호
    var bkNum: Int = 0 // 백업 표시기 번호

    private val _lastCallList = MutableLiveData<List<LastCall>>(emptyList()) // 지난 호출 번호 리스트;
    val lastCallList: LiveData<List<LastCall>> get() = _lastCallList

    fun updateLastCallList(newList: List<LastCall>) {
        _lastCallList.postValue(newList)
    }

    private val _subList = MutableLiveData<List<LastCall>>(emptyList()) // 지난 호출 번호 리스트
    val subList: LiveData<List<LastCall>> get() = _subList

    fun addSubList(newLastCall: LastCall) {
        val currentList = _subList.value?.toMutableList() ?: mutableListOf()
        currentList.add(newLastCall)
        if (currentList.size > 4) currentList.removeAt(0) // 4개 이상인 경우 첫 번째 요소 제거
        _subList.postValue(currentList.toList())
    }

    // 지난 호출 번호 리스트
    var soundList: ArrayList<Sound> = ArrayList() // 음성 설정

    var playVideo: Int = 0 // 동영상 재생 여부
    var playSub: Int = 0 // 보조화면 사용 여부

    private val _tellerMent = MutableLiveData("") // 하단 안내 문구
    val tellerMent: LiveData<String> get() = _tellerMent
    fun updateTellerMent(tellerMent: String) {
        _tellerMent.postValue(tellerMent)
    }

    private val _isCrowded = MutableLiveData(false) // 혼잡여부 BOOL
    val isCrowded: LiveData<Boolean> get() = _isCrowded
    fun updateCrowded(isCrowded: Boolean) {
        _isCrowded.postValue(isCrowded)
    }
    var crowdedMsg: String = "" // 혼잡 메세지

    // 볼륨 테스트 데이터
    var volumeWin: Int = 0 // 볼륨테스트 창구 번호
    var callVolumeSize: Int = 0 // 호출시 볼륨 크기
    var ticketVolumeSize: Int = 0 // 발권시 볼륨 크기
    var volumeName: String = "" // 벨소리 파일 명
    var playNum: Int = 0 // 재생 횟수
    var infoSound: Int = 0 // 안내 음성 종류 0 : 창구로 오십시오, 1 : 창구로 모시겠습니다. 2: 창구에서 도와드리겠습니다.

    // 음성 설정
    var conseling: String = "" // 상담 유무

    // 동영상 관리
    var mainDisplayTime: Int = 0 // 메인화면 표시 시간
    var subDisplayTime: Int = 0 // 보조화면 표시 시간
    var adDisplayTime: Int = 0 // 홍보화면 표시 시간
    var winDisplayTime: Int = 0 // 창구별 표시 시간, 안쓰는것으로 보인다.
    var adFileList: Array<String> = emptyArray() // 홍보물 리스트
    var iSVideo: Int = 0 // 기본 동영상 유무 1: 기본 동영상, 2: 지점 설정 동영상

    // 상담 예약 관련
    var reserveList: ArrayList<Reserve> = ArrayList() // 예약 리스트

    // 상담 예약 호출
    var reserveNum: String = "" // 예약 번호
    var reserveTime: Int = 0 // 예약 시간
    var customerNum: String = "" // 고객 번호
    var customerName: String = "" // 고객 이름
    var reserveError: String = "" // 장애 여부
    var reserveCallNum: String = "" // 호출 번호
    var reserveWinID: String = "" // 호출 창구 ID
    var reserveWinNum: String = "" // 호출 창구 번호
    var reserveBackNum: String = "" // 호출 보조 창구 번호
    var reserveBackWay: String = "" // 호출 백업 표시 방향

    // 메인 표시기 상태
    var mainWinNum: Int = 0 // 메인 표시기 창구 번호

    var testVolume = TestVolume()

    // 화면표시 정보
    fun setDisplayInfo(packetData: String) {
        // data ex) DATA#설치#표시기색상테마, 대기인수표시여부 $
        val cleanedData = packetData.removeChar("$").removeChar("DATA#설치#")
        val displayInfo = cleanedData.splitData(";")

        theme = displayInfo.getOrNull(0)?.toIntOrNull() ?: 1
        availableWaitInfo = displayInfo.getOrNull(1)?.toIntOrNull() ?: 1

        Log.d("표시기색상테마 : $theme 대기인수표시여부 : $availableWaitInfo")
    }

    // 볼륨 테스트
    fun setVolumeTest(packetData: String): TestVolume {
        // data ex) DATA#호출볼륨#창구번호;테스트볼륨;벨소리파일명;재생횟수;안내음성종류;$
        val cleanedData = packetData.removeChar("$").removeChar("DATA#호출볼륨#")
        val splitData = cleanedData.splitData(";") // ;로 나눔

        val volumeWin   = splitData[0].toIntOrNull() ?: 0
        val volumeSize  = splitData[1].toIntOrNull() ?: 0
        val volumeName  = splitData[2]
        val playNum     = splitData[3].toIntOrNull() ?: 0
        val infoSound   = splitData[4].toIntOrNull() ?: 0

        testVolume = TestVolume(volumeWin, volumeSize, volumeName, playNum, infoSound)

        return testVolume
    }

    // 동영상 정보
    fun setVideoInfo(data: String) {
        Log.d("Data: $data")
        val data1 = data.splitData("\\$")

        iSVideo = 0

        for (i in data1.indices) {
            val cleanedData = data1[i].removeChar("DATA_NEW#")
            val data2 = cleanedData.splitData("#")

            when (data2[0]) {
                "표시" -> {
                    val data3 = data2[1].splitData("&")
                    playVideo = data3.getOrNull(0)?.toIntOrNull() ?: 0
                    mainDisplayTime = (data3.getOrNull(1)?.toIntOrNull() ?: 0) * 1000
                    subDisplayTime = (data3.getOrNull(2)?.toIntOrNull() ?: 0) * 1000
                    iSVideo = data3.getOrNull(4)?.toIntOrNull() ?: 0

                    val data4 = data3[3].splitData(";")
                    for (j in data4.indices) {
                        val data5 = data4[j].splitData("=")
                        winDisplayTime = if (data5.getOrNull(0)?.toIntOrNull() == winID) {
                            data5[1].toIntOrNull() ?: 0
                        } else {
                            0
                        }
                    }
                }
                "동영상" -> {
                    adFileList = if (data2.size > 1) {
                        data2[1].splitData("&")
                    } else {
                        emptyArray()
                    }
                }
                else -> {
                    Log.d("default")
                }
            }
        }
    }

    // 음성 설정
    fun soundSet(data: String) {
        soundList.clear()
        Log.d("Data : $data")

        val data1 = data.splitData("\\$") // $ 로 잘라서 가져옴
        for (item in data1) {
            val cleanedItem = item.removeChar("DATA#") // Data# 삭제
            val data2 = cleanedItem.splitData("#") // 창구,호출,상담별로 끊어옴

            when (data2.getOrNull(0)) {
                "창구" -> {
                    val data3 = data2.getOrNull(1)?.splitData("&") ?: emptyArray()
                    for (item3 in data3) {
                        val data4 = item3.splitData(";")
                        val nSound = data4.getOrNull(0)?.toIntOrNull() ?: 0
                        val sound = Sound(nSound, data4.getOrNull(1) ?: "", data4.getOrNull(2) ?: "")
                        soundList.add(sound)
                    }
                }
                "볼륨" -> {
                    val data3 = data2.getOrNull(1)?.splitData(";") ?: emptyArray()
                    volumeInfo = data3.getOrNull(1) ?: "" // 호출시 볼륨 사이즈
                }
                "호출" -> {
                    val data3 = data2.getOrNull(1)?.splitData(";") ?: emptyArray()
                    bellInfo = data3.getOrNull(0) ?: "" // 파일명
                    callInfo = data3.getOrNull(1) ?: "" // 호출 횟수

                    ment = when (data3.getOrNull(2)?.toIntOrNull()) {
                        0 -> "창구로 오십시오."
                        1 -> "창구로 모시겠습니다."
                        else -> "창구에서 도와드리겠습니다."
                    }
                }
                "상담" -> {
                    val data3 = data2.getOrNull(1)?.splitData(";") ?: emptyArray()
                    conseling = data3.getOrNull(0) ?: "" // 상담예약 출력 여부
                }
                else -> Log.d(" 디폴트 : ${data2.getOrNull(1)}")
            }
        }
    }

    // 호출 취소
    fun setCallCancel(param : CallCancelData) {
        this.cancelError    = param.cancelError
        this.cancelcallNum  = param.cancelCallNum
        this.ticketWinID    = param.ticketWinID
        this.callWinID      = param.callWinID
        this.callWinNum     = param.callWinNum
        this.bkNum          = param.bkNum
        updateWaitNum(param.wait)

        val newLastCallList = param.lastCallNumList.splitData("#")
            .mapNotNull { item ->
                val data1 = item.splitData(";")
                if (data1.size == 5) {
                    try {
                        LastCall(
                            data1[0].toInt(), data1[1].toInt(), data1[2].toInt(), data1[3].toInt(), data1[4].toInt()
                        )
                    } catch (e: NumberFormatException) {
                        Log.e("Call Cancel Failed: ${e.message}")
                        null
                    }
                } else {
                    null
                }
            }.toCollection(ArrayList())

        updateLastCallList(newLastCallList)
    }

    // 호출 및 재호출
    // [2023.05.17][modify kimhj] VIP실 음성 대응
    //public boolean setcallNum(int errorStatus,int callNum,int ticketWinID,int callWinID,int winWaitNum,int callWinNum,String lastCallNum,int callBkDisplay,int callBkWay,int reserve)
    fun setCallNum(
        param: CallRequestData
    ): Boolean {
        try {
            this.errorStatus    = param.errorStatus
            this.ticketWinID    = param.ticketWinID
            this.callWinID      = param.callWinID
            this.callWinNum     = param.callWinNum
            this.callBkDisplay  = param.bkDisplay
            this.callBkWay      = param.bkWay
            this.reserve        = param.reserve
            this.flagVIP        = param.vipFlag // [2023.05.17][add kimhj]
            updateWaitNum(param.winWaitNum)
            updateCallNum(param.callNum)

            val newLastCallList = ArrayList<LastCall>()

            val data = param.lastCallNum.splitData("#")
            for (i in data.indices) {
                val data1 = data[i].splitData(";")
                if (data1.size == 5) {
                    val lastCall = LastCall(
                        data1[0].toInt(), data1[1].toInt(), data1[2].toInt(), data1[3].toInt(), data1[4]
                            .toInt()
                    )
                    newLastCallList.add(lastCall)
                } else {
                    Log.w("Last Call Num Length is not 5")
                }
            }
            updateLastCallList(newLastCallList)

            val subCall = LastCall(param.ticketWinID, param.callWinID, param.callWinNum, param.callNum, param.winWaitNum)
            addSubList(subCall)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("callNum Exception")
            return false
        }
        return true
    }

    // 혼잡 설정
    fun setCrowed(isCrowded: Int, crowdedMsg: String) {
        updateCrowded(isCrowded == 1)
        this.crowdedMsg = crowdedMsg
    }

    // 부재 정보 표시
    fun setEmpty(flagEmpty: Int, emptyMsg: String) {
        updateFlagEmpty(flagEmpty)
        this.emptyMsg = emptyMsg
    }

    // 직원 정보 리스트
    /*fun setTellerList(packetData: String) {
        this.tellerList.clear()
        //데이터를 보지 못하여 확실치는 않으나 &까지 잘라내면 각각의 직원정보가 되는것으로 추정됨.
        val data1 = packetData.splitData("DATA")    // 0 : 공백, 1: 직원정보 2: 화면당 표시수
        val data2 = data1.getOrNull(1)?.splitData("#") ?: return
        val tellerDatas = data2.getOrNull(2)?.splitData("&") ?: return    // 직원 단위로 자름
        var empty = ""

        for (i in tellerDatas.indices) {
            val tellerData = tellerDatas[i].splitData(";")
            if (tellerData.size < 18) {
                Log.d("Data length < 18")
                Log.w("Data length < 18. i = $i")
                continue
            }

            Log.d(
                "직원 IDX : ${tellerData[0]} 소속 창구 ID ${tellerData[1]} 직무명 ${tellerData[2]} 직원 사진 : ${tellerData[3]} " +
                        "직원 PC IP : ${tellerData[4]} 백업 표시기 번호 : ${tellerData[5]} 백업 화살표 방향 : ${tellerData[6]} " +
                        "프로필1 : ${tellerData[7]} 프로필2 : ${tellerData[8]} 직원 행번 : ${tellerData[9]} 표시기 IP : ${tellerData[10]} " +
                        "직원명 : ${tellerData[11]} 공석 여부 : ${tellerData[12]} 창구명 : ${tellerData[13]} 창구 번호 : ${tellerData[14]} " +
                        "부재 메세지 ${tellerData[15]}"
            )

            try {
                val teller = Teller(
                    tellerID = tellerData[0].toInt(),
                    winID = tellerData[1].toInt(),
                    job = tellerData[2],
                    tellerImg = tellerData[3],
                    pcIP = tellerData[4],
                    bkDisplay = tellerData[5].toInt(),
                    bkWay = tellerData[6].toInt(),
                    proFile1 = tellerData[7],
                    proFile2 = tellerData[8],
                    tellerNum = tellerData[9].toInt(),
                    displayIP = tellerData[10],
                    tellerName = tellerData[11],
                    pjt = tellerData[12].toInt(),
                    winName = tellerData[13],
                    winNum = tellerData[14].toInt(),
                    emptyMsg = tellerData[15],
                    vip = tellerData[16],
                    dispatch = tellerData[17]
                )
                this.tellerList.add(teller)

                empty = tellerData[15]

                // 디스플레이 IP 가 같을 경우
                if (Const.CommunicationInfo.MY_IP == tellerData[10]) {
                    emptyMsg = empty
                    this.teller = teller
                }

                try {
                    // 메인 표시기 공석 여부 체크
                    if (winNum == tellerData[5].toInt()) {      //tellerData[5] : 백업표시기 번호
                        mainWinNum = tellerData[14].toInt()     //tellerData[14] : 창구번호
                        mainPJT = putPJT(tellerData[12])        //tellerData[12] : 공석여부
                    }
                } catch (e: Exception) {
                    Log.e("Failed Empty check")
                    break
                }
            } catch (e: Exception) {
                Log.e("Failed Set Teller info")
                break
            }
        }

        if (data1.size > 2) {
            val dData1 = data1[2].splitData("#")
            try {
                this.display = dData1[2].removeChar("&$").toInt()
            } catch (e: Exception) {
                this.display = 0
            }
        }
    }*/
    /**
     * param : tellerList는 다음의 형식으로 들어온다.
     * DATA#직원#38;2;;;10.131.54.65;0;1;|;|;30702819;1.1.1.3;구민가;사용중;일반업무;1;;0;0;&39;3;;;10.131.54.65;1;1;|;|;40732950;1.1.1.2;영은가;사용중;상담업무;2;;0;0;&41;3;;;10.131.54.65;2;1;|;|;41704752;1.1.1.2;이보가;사용중;상담업무;3;;0;0;&40;1;;;10.131.54.85;1;1;|;|;41100248;1.1.1.2;팔윤가;사용중;입출금/제신고;4;;0;0;&37;1;;;10.131.54.85;1;1;|;|;29111781;1.1.1.2;일시가;사용중;입출금/제신고;5;;0;0;&$
     */
    // 직원 정보 리스트 설정
    fun setTellerList(tellerList: ArrayList<Teller>) {
        this.tellerList.clear()
        this.tellerList.addAll(tellerList)
    }
    fun setTellerList(tellerList: String) {
        this.tellerList.clear()

        val splitByData = tellerList.splitData("DATA") // 0: 공백, 1: 직원정보 2: 화면당 표시수
        val splitByHash = splitByData.getOrNull(1)?.splitData("#") ?: return
        val tellerInfoList = splitByHash.getOrNull(2)?.splitData("&") ?: return // 직원 단위로 자름
        var empty = ""

        // 직원 정보를 ArrayList에 등록
        for ((i, tellerInfo) in tellerInfoList.withIndex()) {
            val tellerData = tellerInfo.splitData(";")
            if (tellerData.size < 18) {
                Log.d("Data length < 18")
                Log.d("Data length < 18. i = $i")
                continue
            }

            Log.d(
                "직원 IDX : ${tellerData[0]} 소속 창구 ID ${tellerData[1]} 직무명 ${tellerData[2]} 직원 사진 : ${tellerData[3]} " +
                    "직원 PC IP : ${tellerData[4]} 백업 표시기 번호 : ${tellerData[5]} 백업 화살표 방향 : ${tellerData[6]} " +
                    "프로필1 : ${tellerData[7]} 프로필2 : ${tellerData[8]} 직원 행번 : ${tellerData[9]} 표시기 IP : ${tellerData[10]} " +
                    "직원명 : ${tellerData[11]} 공석 여부 : ${tellerData[12]} 창구명 : ${tellerData[13]} 창구 번호 : ${tellerData[14]} " +
                    "부재 메세지 ${tellerData[15]}"
            )

            // 불필요한 try-catch 블록 제거, tellerData의 유효성을 먼저 검사
            val teller = Teller(
                tellerID =  tellerData[0].toIntOrNull() ?: 0,
                winID =     tellerData[1].toIntOrNull() ?: 0,
                job =       tellerData[2],
                tellerImg = tellerData[3],
                pcIP =      tellerData[4],
                bkDisplay = tellerData[5].toIntOrNull() ?: 0,
                bkWay =     tellerData[6].toIntOrNull() ?: 1, // 기본값 1 설정
                proFile1 =  tellerData[7],
                proFile2 =  tellerData[8],
                tellerNum = tellerData[9].toIntOrNull() ?: 0,
                displayIP = tellerData[10],
                tellerName = tellerData[11],
                pjt =       putPJT(tellerData[12]), // putPJT 함수의 반환 타입에 따라 필요한 경우 변환 로직 추가
                winName =   tellerData[13],
                winNum =    tellerData[14].toIntOrNull() ?: 0,
                emptyMsg =  tellerData[15],
                vip =       tellerData[16],
                dispatch =  tellerData[17]
            )

            this.tellerList.add(teller)
            empty = tellerData[15]

            if (Const.CommunicationInfo.MY_IP == tellerData[10]) {
                // 디스플레이 IP가 같을 경우
                emptyMsg    = empty
                tellerID    = teller.tellerID
                winID       = teller.winID
                job         = teller.job
                imgName     = teller.tellerImg
                pcIP        = teller.pcIP
                bkDisplay   = teller.bkDisplay
                bkWay       = teller.bkWay
                profile1    = getProFile(teller.proFile1)
                profile2    = getProFile(teller.proFile2)
                tellerNum   = teller.tellerNum
                displayIP   = teller.displayIP
                tellerName  = teller.tellerName
                winName     = teller.winName
                winNum      = teller.winNum
                updatePjt(teller.pjt)
            }

            // 메인 표시기 공석 여부 체크 (try-catch 블록 제거, tellerData의 유효성을 먼저 검사)
            val backupDisplayNumber = tellerData[5].toIntOrNull()
            if (backupDisplayNumber != null && winNum == backupDisplayNumber) {
                mainWinNum = tellerData[14].toIntOrNull() ?: 0
                //mainPJT = putPJT(tellerData[12]) //쓰는곳이 없음
            }
        }

        // 직원 한 명의 정보를 수정 뒤 적용했을 경우 Display 정보가 오지 않음, 예외 처리
        if (splitByData.size > 2) {
            val dData1 = splitByData[2].splitData("#")
            display = dData1.getOrNull(2)?.removeChar("&$")?.toIntOrNull() ?: 0
        }
    }

    private fun putPJT(isPJT: String): Int {
        return when (isPJT) {
            "공석", "공석(PJT)" -> 1
            else -> 0
        }
    }

    // 프로파일에서 | 부분 제외(초급,중급,고급 제외)
    private fun getProFile(data: String): String {
        // 일반 | 는 boolean과 같은 결과를 리턴 하여 OR 조건에 걸리기 때문에 \\| 특수문자로 인식하도록 함
        val splitString = data.splitData("\\|")
        return splitString[0]
    }

    // 각 창구별 번호, 이름, 대기인원 리스트 저장
    fun setWinList(winIdListStr: String, winNameListStr: String, winWaitListStr: String) {
        val winIdList = winIdListStr.splitData(";")
        val winNameList = winNameListStr.splitData(";")
        val winWaitList = winWaitListStr.splitData(";")

        winList.clear()

        if (winIdList.size == winNameList.size && winIdList.size == winWaitList.size) {
            Log.d(winIdList.size.toString())
            winIdList.forEachIndexed { i, winId ->
                try {
                    val winWait = WinWait(winId.toInt(), winNameList[i], winWaitList[i].toInt())
                    winList.add(winWait)
                    if (winId.toInt() == winID) {
                        updateWaitNum(winWaitList[i].toInt())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("Failed SetWinList")
                }
            }
        }
    }

    // 접속 승인시 받아온 직원 정보를 분해 하여 저장
    // 접속 승인 시 받아온 직원 정보를 분해하여 저장
    fun setTellerInfo(infoString: String) {
        val splitData = infoString.splitData(";") // ;으로 문자열을 자름
        Log.d("InfoString = $infoString")

        // 데이터가 없이 올 경우 기본값으로 설정 (불필요한 변수 초기화 제거)
        if (splitData.size >= 18) {
            tellerID    = splitData[0].toIntOrNull() ?: 0
            winID       = splitData[1].toIntOrNull() ?: 0
            imgName     = splitData[3]
            //빠진 데이터는 뭐지..
            bkDisplay   = splitData[5].toIntOrNull() ?: 0 
            bkWay       = splitData[6].toIntOrNull() ?: 1 

            val pjt     = splitData[12].toIntOrNull()?.minus(1) ?: 1
            updatePjt(pjt)

            displayIP   = splitData[10] // 표시기 IP
            tellerName  = splitData[11] // 직원명
            winName     = splitData[13] // 소속 창구명
            winNum      = splitData[14].toIntOrNull() ?: 0 
            flagVIP     = splitData[16].toIntOrNull() ?: 0 
        } else {
            // 텔러 정보 길이 오류 처리 (필요에 따라 추가적인 로직 구현)
            Log.e("Teller info length error: ${splitData.size}")
        }

        Log.d("TellerInfo : $infoString")
        Log.d("WinID : $winID")
    }

    // 화면 설정 정보
    fun setScreenInfo(param: AcceptAuthResponseData) {
        val wait = param.settingInfo
        val mentNum = param.reserve1
        val isAbsence = param.reserve2// 전산 장애 표시, 부재중데이터로 보임
        //val dontcare = param.reserve3 // 공석 표시, 기존코드에서 사용하지 않고 있었음.

        Log.d(wait)
        Log.d(mentNum)
        Log.d(isAbsence)

        // 안내 멘트 설정
        ment = when (mentNum) {
            "0" -> "창구로 오십시오."
            "1" -> "창구로 모시겠습니다."
            "2" -> "창구에서 도와드리겠습니다."
            else -> ""
        }

        val splitWaitData = wait.splitData(";")

        if (splitWaitData.isNotEmpty()) {
            availableWaitInfo = if (splitWaitData[0] == "T" || splitWaitData[0] == "1") 1 else 0
        }
        if (splitWaitData.size > 1) {
            updateTellerMent(splitWaitData[1])
        }

        val splitAbsenceData = isAbsence.splitData(";")
        var flagEmpty = 0
        if (splitAbsenceData.isNotEmpty()) {
            flagEmpty = if (splitAbsenceData[0].isEmpty() || splitAbsenceData[0] == "1") 1 else 0
            updateFlagEmpty(flagEmpty)
        }
        emptyMsg = ""
        if (splitAbsenceData.size > 1) {
            emptyMsg = splitAbsenceData[1]
            if (flagEmpty == 1 && emptyMsg.isEmpty()) {
                emptyMsg = "부재중"
            }
        }

        this.bellInfo = param.bellInfo
        this.callInfo = param.callInfo
        this.volumeInfo = param.volumeInfo

        Log.d("Media Info : ${param.mediaInfo}")

        try {
            val slideMediaInfo = param.mediaInfo.splitData("#") // 모드, 메인화면, 보조화면, 홍보화면 시간, 파일명으로 분할
            Log.d("Media Info : ${param.mediaInfo} length : ${slideMediaInfo.size}")


            mainDisplayTime = slideMediaInfo.getOrNull(0)?.toIntOrNull() ?: 30 // 기본값 30 설정
            playSub = slideMediaInfo.getOrNull(1)?.toIntOrNull() ?: 0 // 보조 사용 여부
            subDisplayTime = (slideMediaInfo.getOrNull(2)?.toIntOrNull() ?: 0)
            if (playSub == 0) subDisplayTime = 0
            playVideo = slideMediaInfo.getOrNull(3)?.toIntOrNull() ?: 0 // 동영상 사용 여부
            adDisplayTime = (slideMediaInfo.getOrNull(4)?.toIntOrNull() ?: 0)
            if (playVideo == 0) adDisplayTime = 0

            // 240115, by HAHU  초기화 시키고 수행. 앱 재시작 안 하고 패킷이 올 수도 있어서
            adFileList = emptyArray()

            Log.d("MediaInfo length : ${slideMediaInfo.size}")
            if (slideMediaInfo.size >= 6) {
                adFileList = slideMediaInfo[5].splitData(";") // ;로 구분하여 파일리스트 저장
            }
        } catch (e: Exception) {
            Log.d("Media Info error $e")
            playVideo = 0
            mainDisplayTime = 30
            subDisplayTime = 0
            adDisplayTime = 0
            adFileList = emptyArray()
        }

        // 231130, by HAHU 음성 호출기인 경우 화면 전환 할 필요가 없어서 설정 초기화
        if (Const.CommunicationInfo.CALLVIEW_MODE == "3") {
            Log.d("Media Info 초기화")
            playVideo = 0
            mainDisplayTime = 30
            subDisplayTime = 0
            adDisplayTime = 0
            adFileList = emptyArray()
        }
    }

    fun setError(errorInfo: String) {
        val dataList = errorInfo.splitData("#")

        if (dataList[1] == "설치") {
            val dataError = dataList[2].removeChar("$")
            val systemError = dataError.toIntOrNull() ?: 1 // 0: 정상 운영, 1: 전산 장애, 변환 실패 시 기본값 1 설정
            updateSystemError(systemError)
        }
    }

    fun setWaitResponse(ticketNum: Int, waitNum: Int) {
        this.ticketNum = ticketNum
        updateWaitNum(waitNum)
    }

    // 예약 리스트 응답
    fun setReserveList(mul: Int, data: String) {
        Log.d("SetReserveList DATA : $data")
        Log.d("mul : $mul")

        if (data.length < 10) return // 데이터 길이가 너무 짧은 경우, early return

        reserveList.clear()

        val dataList1 = data.splitData("&") // &로 나눔
        dataList1.forEach { reservationData ->
            try {
                val dataList2 = reservationData.splitData("#")

                val day         = dataList2[0]
                val branchNum   = dataList2[1]
                val reserveNum  = dataList2[2]
                val reserveTime = dataList2[3].removeChar(":").toInt()
                val customerNum = dataList2[4]
                val customerName = dataList2[5]
                val customerTel = dataList2[6]
                val customerGrade = dataList2[7]
                val tellerNum   = dataList2[8]
                val tellerName  = dataList2[9]
                val tellerJob   = dataList2[10]
                val resvWinID   = dataList2[11].toInt()
                val winName     = dataList2[12]
                val arriveTime  = dataList2[13]
                val isArrive    = dataList2[14]
                val callTime    = dataList2[15]
                val isCancel    = dataList2[16]

                // - 20201229 by YOUNG. 소속 창구 예약 정보만 리스트 생성
                if (winID == resvWinID) {
                    val reserve = Reserve(
                        day, branchNum, reserveNum, reserveTime, customerNum, customerName, customerTel,
                        customerGrade, tellerNum, tellerName, tellerJob, resvWinID, winName, arriveTime,
                        isArrive, callTime, isCancel
                    )
                    reserveList.add(reserve) 

                    Log.d("예약리스트 추가 - 예약번호 : $reserveNum")
                    Log.d(
                        "예약일 : $day 지점번호 : $branchNum 예약 번호 : $reserveNum 예약 시간 : $reserveTime 고객 번호 : $customerNum 고객 이름 : $customerName 고객 연락처 : $customerTel " +
                                "고객 등급 : $customerGrade 상담직원 번호 : $tellerNum 상담직원 이름 : $tellerName 상담 업무 : $tellerJob 창구 ID : $resvWinID 창구명 : $winName 도착 시간 : $arriveTime " +
                                "도착 여부 : $isArrive 호출 시간 : $callTime 취소여부 : $isCancel"
                    )
                } else {
                    Log.d("예약 번호 : $reserveNum 창구 ID : $resvWinID 다른창구로 리스트 추가 안함 - 소속 창구ID : $winID")
                }
            } catch (e: Exception) {
                Log.e("Failed reserve List")
            }
        }
        compareToList()
    }

    private fun compareToList() {
        // Comparator를 사용한 정렬 (원본 자바 코드와 동일한 동작)
        reserveList.sortWith(Comparator { vo1, vo2 -> vo1.reserveTime - vo2.reserveTime })
    }

    private fun isNullString(data: String): String {
        return data.ifEmpty { "10" }
    }

    fun setAddReserve(data: String) {
        Log.d("SetAddReserve Data: $data") // 로그 출력

        try {
            val dataList1 = data.splitData("#")

            // 데이터 유효성 검사
            if (dataList1.size <= 11) {
                Log.d("Failed SetAddReserve: Invalid data format")
                return
            }

            // 데이터 추출 및 변환
            val day         = dataList1[0]
            val branchNum   = dataList1[1]
            val reserveNum  = dataList1[2]
            val reserveTime = dataList1[3].removeChar(":").toIntOrNull() ?: 0 
            val tellerNum   = dataList1[4]
            val tellerName  = dataList1[5]
            val tellerJob   = dataList1[6]
            val customerNum = dataList1[7]
            val customerName= dataList1[8]
            val customerTel = dataList1[9]
            val customerGrade = dataList1[10]
            val resvWinID = dataList1[12].toIntOrNull() ?: 0 

            // 소속 창구 예약 정보만 리스트에 추가
            if (winID == resvWinID) {
                val reserve = Reserve(
                    day, branchNum, reserveNum, reserveTime, customerNum, customerName, customerTel,
                    customerGrade, tellerNum, tellerName, tellerJob, winID, winName, "00:00:00", "N", "00:00:00", "N"
                )
                reserveList.add(reserve) 
                compareToList()
            }
        } catch (e: Exception) {
            Log.e("Failed SetAddReserve")
        }
    }

    // 예약 추가 요청
    fun setUpdateReserve(data: String) {
        try {
            Log.d("SetUpdateReserve Data : $data")
            val dataList1   = data.splitData("#")
            val day         = dataList1[0]              // 예약 일자
            val branchNum   = dataList1[1]
            val reserveNum  = dataList1[2]
            val reserveTime = dataList1[3].removeChar(":").toIntOrNull() ?: 0
            val tellerNum   = dataList1[4]
            val tellerName  = dataList1[5]
            val tellerJob   = dataList1[6]
            val customerNum = dataList1[7]
            val customerName= dataList1[8]
            val customerTel = dataList1[9]
            val customerGrade = isNullString(dataList1[10])
            val winID = if (dataList1.size > 11) dataList1[12].toIntOrNull() ?: 0 else 0

            Log.d(
                "예약 일자 : $day 지점 번호 : $branchNum 예약 번호 : $reserveNum 예약 일자 : $reserveTime 직원 번호 : $tellerNum 직원 명 : $tellerName " +
                        "업무 명 : $tellerJob 고객 번호 : $customerNum 고객 이름 : $customerName 고객 연락처 : $customerTel 고객 등급 : $customerGrade 창구 ID : $winID"
            )

            val reserveToUpdate = reserveList.find { it.reserveNum == reserveNum }
            reserveToUpdate?.updateReserve(
                day, branchNum, reserveNum, reserveTime, tellerNum, tellerName,
                tellerJob, customerNum, customerName, customerTel, customerGrade, winID
            )
            compareToList()
        } catch (e: Exception) {
            Log.e("Failed SetUpdateReserve")
        }
    }

    // 예약 취소 요청
    fun setCancelReserve(data: String) {
        Log.d("SetCancel Data : $data")

        try {
            val dataList1   = data.splitData("#")
            val day         = dataList1[0]              // 예약 일자
            val branchNum   = dataList1[1].toIntOrNull() ?: 0 
            val reserveNum  = dataList1[2]
            val reserveTime = dataList1[3].removeChar(":").toIntOrNull() ?: 0 
            val tellerNum   = dataList1[4].toIntOrNull() ?: 0 
            val tellerName  = dataList1[5]
            val tellerJob   = dataList1[6]
            val customerNum = dataList1[7]
            val customerName= dataList1[8]
            val customerTel = dataList1[9]
            val customerGrade = isNullString(dataList1[10]).toIntOrNull() ?: 10

            Log.d(
                "예약 일자 : $day 지점 번호 : $branchNum 예약 번호 : $reserveNum 예약 일자 : $reserveTime 직원 번호 : $tellerNum 직원 명 : $tellerName " +
                        "업무 명 : $tellerJob 고객 번호 : $customerNum 고객 이름 : $customerName 고객 연락처 : $customerTel 고객 등급 : $customerGrade"
            )

            val reserveToCancel = reserveList.find { it.reserveNum == reserveNum }
            reserveToCancel?.cancelReserve()
        } catch (e: Exception) {
            Log.e("Failed SetCancle Reserve")
        }
    }

    // 예약 고객 호출 요청
    fun setCallReserve(data: String): Int {
        Log.d("SetCallReserve Data : $data")

        return try {
            val dataList1 = data.splitData("#")

            // 데이터 유효성 검사
            if (dataList1.size < 11) {
                Log.w("Failed SetCallReserve: Invalid data format")
                return 100
            }

            // 데이터 추출 및 변환
            val reserveNum  = dataList1[0]
            val reserveTime = dataList1[1].removeChar(":").toIntOrNull() ?: 0
            val customerNum = dataList1[2]
            val customerName= dataList1[3]
            val error       = dataList1[4]
            val callNum     = dataList1[5]
            val winID       = dataList1[6]
            val winNum      = dataList1[7]
            val backNum     = dataList1[8]
            val backWay     = dataList1[9]
            val vipFlag     = dataList1[10].toIntOrNull() ?: 0

            this.reserveNum     = reserveNum
            this.reserveTime    = reserveTime
            this.customerNum    = customerNum
            this.customerName   = customerName
            this.reserveError    = error
            this.reserveCallNum = callNum
            this.reserveWinID    = winID
            this.reserveWinNum   = winNum
            this.reserveBackNum  = backNum
            this.reserveBackWay  = backWay
            this.flagVIP = vipFlag
            val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            Log.d("현재 시간 : $nowTime")

            val reserveToUpdate = reserveList.find { it.reserveNum == reserveNum }
            reserveToUpdate?.callTime = nowTime

            reserveWinNum.toIntOrNull() ?: 100
        } catch (e: Exception) {
            Log.e("Failed SetCall Reserve")
            100
        }
    }

    fun setReserveArrive(strData: String) {
        Log.d("setReserveArrive : $strData")
// 2019-12-12#0000#2019121200009008#14:30:00#CUST0123456789#김고객님#01012345566#3###개인대출상담#1#종합상담창구#14:18:32#Y#00:00:00#N
        try {
            val dataList = strData.splitData("#")

            // 데이터 유효성 검사
            if (dataList.size < 17) {
                Log.w("Failed setReserveArrive: Invalid data format")
                return
            }

            // 데이터 추출 및 변환
            val reserveDay = dataList[0]
            val ticketNum = dataList[1]
            val reserveNum = dataList[2]
            val reserveTime = dataList[3].removeChar(":").toIntOrNull() ?: 0
            val customerNum = dataList[4]
            val customerName = dataList[5]
            val customerTel = dataList[6]
            val value1 = dataList[7]
            val value2 = dataList[8]
            val value3 = dataList[9]
            val work = dataList[10]
            val winNum = dataList[11].toIntOrNull() ?: 0
            val winName = dataList[12]
            val arriveTime = dataList[13]
            val isArrive = dataList[14]
            val callTime = dataList[15]
            val isCancel = dataList[16]

            val reserveToUpdate = reserveList.find { it.reserveNum == reserveNum }
            reserveToUpdate?.setArriveTime(arriveTime, isArrive)
        } catch (e: Exception) {
            Log.e("SetReserveArrive")
        }
    }

    // 직원 정보 갱신
    fun setRenewTeller(winNum: Int, tellerNum: Int, tellerName: String) {
        Log.d("SetRenew Teller")

        this.tellerName = tellerName
        this.tellerNum = tellerNum
    }
}