package com.kct.iqsdisplayer.ui

import android.os.Handler
import android.os.Looper
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.Const.CallViewMode
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.util.Log

object FragmentFactory {
    @IntDef(
        Index.NONE,                 //최초실행 아직 Fragment없음
        Index.FRAGMENT_READY,       //init, TCP접속과정 및 업데이트 설치를 표시하는 용도 UI
        Index.FRAGMENT_SETTING,     //setting
        Index.FRAGMENT_MAIN,        //메인
        Index.FRAGMENT_BACKUP_CALL, //백업호출
        Index.FRAGMENT_MOVIE,       //동영상표출
        Index.FRAGMENT_RECENT_CALL, //최근응대고객
        Index.FRAGMENT_RESERVE_CALL,//예약호출 화면
        Index.FRAGMENT_RESERVE_LIST,//예약리스트 화면
        Index.FRAGMENT_SUB          //보조표시기 모드
    )

    annotation class Index {
        companion object {
            const val NONE: Int                  = 0
            const val FRAGMENT_READY: Int        = NONE + 1
            const val FRAGMENT_SETTING: Int      = FRAGMENT_READY + 1
            const val FRAGMENT_MAIN: Int         = FRAGMENT_SETTING + 1
            const val FRAGMENT_BACKUP_CALL: Int  = FRAGMENT_MAIN + 1
            const val FRAGMENT_MOVIE: Int        = FRAGMENT_BACKUP_CALL + 1
            const val FRAGMENT_RECENT_CALL: Int  = FRAGMENT_MOVIE + 1
            const val FRAGMENT_RESERVE_CALL: Int = FRAGMENT_RECENT_CALL + 1
            const val FRAGMENT_RESERVE_LIST: Int = FRAGMENT_RESERVE_CALL + 1
            const val FRAGMENT_SUB: Int          = FRAGMENT_RESERVE_LIST + 1
        }
    }

    private lateinit var activity: AppCompatActivity
    private var currentIndex    = Index.NONE
    private var beforeIndex     = Index.NONE

    private val fragmentReady        = FragmentReady()
    private val fragmentSetting      = FragmentSetting()
    private val fragmentMain         = FragmentMain()
    private val fragmentBackupCall   = FragmentBackupCall()
    private val fragmentMovie        = FragmentMovie()
    private val fragmentRecentCall   = FragmentRecentCall()
    private val fragmentReserveCall  = FragmentReserveCall()
    private val fragmentReserveList  = FragmentReserveList()
    private val fragmentSub          = FragmentSubPortrait()

    @Index
    fun getCurrentIndex() = currentIndex

    /** 백버튼용으로 추가된 기능으로 한번만 뒤로가기 기능을 구현함 */
    @Index
    fun getBeforeIndex() = beforeIndex

    fun clearBeforeIndex() {
        beforeIndex = Index.NONE
    }

    private fun getCurrentFragment() = getFragment(getCurrentIndex())

    private fun getFragment(@Index index: Int): Fragment? {

        return when (index) {
            Index.FRAGMENT_READY        -> fragmentReady
            Index.FRAGMENT_SETTING      -> fragmentSetting
            Index.FRAGMENT_MAIN         -> fragmentMain
            Index.FRAGMENT_BACKUP_CALL  -> fragmentBackupCall
            Index.FRAGMENT_MOVIE        -> fragmentMovie
            Index.FRAGMENT_RECENT_CALL  -> fragmentRecentCall
            Index.FRAGMENT_RESERVE_CALL -> fragmentReserveCall
            Index.FRAGMENT_RESERVE_LIST -> fragmentReserveList
            Index.FRAGMENT_SUB          -> fragmentSub
            else -> null
        }
    }

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    fun getTagName(@Index index: Int) = when (index) {
        Index.NONE                  -> "NONE"
        Index.FRAGMENT_READY        -> "FragmentReady"
        Index.FRAGMENT_SETTING      -> "FragmentSetting"
        Index.FRAGMENT_MAIN         -> "FragmentMain"
        Index.FRAGMENT_BACKUP_CALL  -> "FragmentBackupCall"
        Index.FRAGMENT_MOVIE        -> "FragmentMovie"
        Index.FRAGMENT_RECENT_CALL  -> "FragmentRecentCall"
        Index.FRAGMENT_RESERVE_CALL -> "fragmentReserveCall"
        Index.FRAGMENT_RESERVE_LIST -> "fragmentReserveList"
        Index.FRAGMENT_SUB          -> "fragmentSub"
        else                        -> "UNKNOWN"
    }

    private fun getDelayTime(hardSetDelayTime: Long = 0) : Long{

        val currentFragmentIndex = getCurrentIndex()
        return if(hardSetDelayTime > 0) hardSetDelayTime else
            when (currentFragmentIndex) {
                Index.FRAGMENT_MAIN         -> ScreenInfo.playTimeMain
                Index.FRAGMENT_RECENT_CALL  -> ScreenInfo.playTimeRecent
                Index.FRAGMENT_MOVIE        -> ScreenInfo.playTimeMedia
                Index.FRAGMENT_RESERVE_CALL -> ScreenInfo.playTimeReserveCall
                Index.FRAGMENT_RESERVE_LIST -> ScreenInfo.playTimeReserveList
                else                        -> ScreenInfo.playTimeMain
            }.toLong()
    }

    /**
     * Fragment를 변경하면 일정시간 뒤에 자동으로 다른 Fragment로 변경됨.
     */
    fun replaceFragment(@Index targetIndex: Int, hardSetDelayTime: Long = 0) {

        val fragmentManager = activity.supportFragmentManager

        if(currentIndex != targetIndex) beforeIndex  = currentIndex

        currentIndex = targetIndex

        val tagName  = getTagName(targetIndex)
        val fragment = getFragment(targetIndex)
        fragment ?: run {
            Log.w("getFragment returned null for index $targetIndex. Skipping fragment replacement.")
            return
        }

        if (fragmentManager.isStateSaved) {
            Handler(Looper.getMainLooper()).post {
                replaceFragment(targetIndex)
            }
            return
        }

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, tagName)
        transaction.commitNow()
        Log.i("화면 변경 : $tagName")
        
        handlerChange.removeCallbacks(runChangeFragment)
        handlerChange.postDelayed(runChangeFragment, getDelayTime(hardSetDelayTime))
    }

    private val handlerChange = Handler(Looper.getMainLooper())

    /** 화면변경 순서는 MAIN -> MOVIE -> RECENT -> RESERVE_LIST 이다 */
    private val runChangeFragment = object : Runnable {
        override fun run() {
            val currentFragmentIndex = getCurrentIndex()

            val isViewModeMain      = Const.ConnectionInfo.CALLVIEW_MODE == CallViewMode.MAIN
            val isAvailableMovie    = ScreenInfo.usePlayMedia && ScreenInfo.mediaFileNameList.size > 0
            val isAvailableRecent   = ScreenInfo.usePlaySub && ScreenInfo.lastCallList.value!!.size > 0
            val isAvailableReserveList   = ScreenInfo.reserveList.size > 0

            if(!isViewModeMain) return

            when(currentFragmentIndex) {
                Index.FRAGMENT_MAIN         -> { //현재화면 대기화면
                    if(isAvailableMovie) replaceFragment(Index.FRAGMENT_MOVIE)
                    else if(isAvailableRecent) replaceFragment(Index.FRAGMENT_RECENT_CALL)
                    else if(isAvailableReserveList) replaceFragment(Index.FRAGMENT_RESERVE_LIST)
                    //else Log.d("Not call, No Movie.. always MainFragment")
                }
                Index.FRAGMENT_MOVIE        -> { //현재화면 동영상화면
                    if(isAvailableRecent) replaceFragment(Index.FRAGMENT_RECENT_CALL)
                    else if(isAvailableReserveList) replaceFragment(Index.FRAGMENT_RESERVE_LIST)
                    else replaceFragment(Index.FRAGMENT_MAIN)
                }
                Index.FRAGMENT_RECENT_CALL  -> { //현재화면 최근응대고객 화면
                    if(isAvailableReserveList) replaceFragment(Index.FRAGMENT_RESERVE_LIST)
                    else replaceFragment(Index.FRAGMENT_MAIN)
                }
                Index.FRAGMENT_RESERVE_LIST -> { //예약리스트
                    replaceFragment(Index.FRAGMENT_MAIN)
                }
                Index.FRAGMENT_RESERVE_CALL -> { //예약호출
                    replaceFragment(Index.FRAGMENT_MAIN)
                }
                Index.FRAGMENT_BACKUP_CALL  -> { //백업호출
                    replaceFragment(Index.FRAGMENT_MAIN)
                }
                else -> { //현재화면 READY, SETTING, 보조표시기 기타등등, 아무처리 없이 해당 화면에 그대로 있는다.
                    Log.d("ChangeFragment - 현재 화면 : ${getTagName(currentFragmentIndex)}")
                    return
                }
            }

            handlerChange.postDelayed(this, getDelayTime())
        }
    }
}