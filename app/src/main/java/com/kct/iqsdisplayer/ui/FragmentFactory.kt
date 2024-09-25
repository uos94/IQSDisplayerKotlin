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
        Index.FRAGMENT_INIT,        //init, TCP접속과정 및 업데이트 설치를 표시하는 용도 UI
        Index.FRAGMENT_SETTING,     //setting
        Index.FRAGMENT_MAIN,        //메인
        Index.FRAGMENT_BACKUP_CALL, //백업호출
        Index.FRAGMENT_MOVIE,       //동영상표출
        Index.FRAGMENT_RECENTCALL   //최근응대고객
    )

    annotation class Index {
        companion object {
            const val NONE: Int                 = 0
            const val FRAGMENT_INIT: Int        = NONE + 1
            const val FRAGMENT_SETTING: Int     = FRAGMENT_INIT + 1
            const val FRAGMENT_MAIN: Int        = FRAGMENT_SETTING + 1
            const val FRAGMENT_BACKUP_CALL: Int = FRAGMENT_MAIN + 1
            const val FRAGMENT_MOVIE: Int       = FRAGMENT_BACKUP_CALL + 1
            const val FRAGMENT_RECENTCALL: Int  = FRAGMENT_MOVIE + 1
        }
    }

    private lateinit var activity: AppCompatActivity
    private var currentIndex = Index.NONE

    private val fragmentInit        = FragmentInit()
    private val fragmentSetting     = FragmentSetting()
    private val fragmentMain        = FragmentMain()
    private val fragmentBackupCall  = FragmentBackupCall()
    private val fragmentMovie       = FragmentMovie()
    private val fragmentRecentCall  = FragmentRecentCall()

    @Index
    fun getCurrentIndex() = currentIndex

    private fun getCurrentFragment() = getFragment(getCurrentIndex())

    private fun getFragment(@Index index: Int): Fragment? {

        return when (index) {
            Index.FRAGMENT_INIT         -> fragmentInit
            Index.FRAGMENT_SETTING      -> fragmentSetting
            Index.FRAGMENT_MAIN         -> fragmentMain
            Index.FRAGMENT_BACKUP_CALL  -> fragmentBackupCall
            Index.FRAGMENT_MOVIE        -> fragmentMovie
            Index.FRAGMENT_RECENTCALL   -> fragmentRecentCall
            else -> null
        }
    }

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    fun getTagName(@Index index: Int) = when (index) {
        Index.NONE                  -> "NONE"
        Index.FRAGMENT_INIT         -> "FragmentInit"
        Index.FRAGMENT_SETTING      -> "FragmentSetting"
        Index.FRAGMENT_MAIN         -> "FragmentMain"
        Index.FRAGMENT_BACKUP_CALL  -> "FragmentBackupCall"
        Index.FRAGMENT_MOVIE        -> "FragmentMovie"
        Index.FRAGMENT_RECENTCALL   -> "FragmentRecentCall"
        else                        -> "UNKNOWN"
    }

    private fun getDelayTime(hardSetDelayTime: Long = 0) : Long{

        val currentFragmentIndex = getCurrentIndex()
        return if(hardSetDelayTime > 0) hardSetDelayTime else
            when (currentFragmentIndex) {
                Index.FRAGMENT_MAIN         -> ScreenInfo.playTimeMain
                Index.FRAGMENT_RECENTCALL   -> ScreenInfo.playTimeSub
                Index.FRAGMENT_MOVIE        -> ScreenInfo.playTimeMedia
                else                        -> ScreenInfo.playTimeMain
            }.toLong()
    }

    /**
     * Fragment를 변경하면 일정시간 뒤에 자동으로 다른 Fragment로 변경됨.
     */
    fun replaceFragment(@Index targetIndex: Int, hardSetDelayTime: Long = 0) {

        val fragmentManager = activity.supportFragmentManager

        currentIndex = targetIndex

        val tagName = getTagName(targetIndex)
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
        transaction.commit()
        Log.i("화면 변경 : $tagName")
        
        handlerChange.removeCallbacks(runChangeFragment)
        handlerChange.postDelayed(runChangeFragment, getDelayTime(hardSetDelayTime))
    }

    private val handlerChange = Handler(Looper.getMainLooper())

    private val runChangeFragment = object : Runnable {
        override fun run() {

            val currentFragmentIndex = getCurrentIndex()

            val isViewModeMain      = Const.ConnectionInfo.CALLVIEW_MODE == CallViewMode.MAIN

            when(currentFragmentIndex) {
                Index.FRAGMENT_MAIN         -> { //현재화면 대기화면
                    if(ScreenInfo.usePlayMedia && isViewModeMain) {
                        replaceFragment(Index.FRAGMENT_MOVIE)
                    } else if(ScreenInfo.usePlaySub) {
                        replaceFragment(Index.FRAGMENT_RECENTCALL)
                    } else {
                        Log.d("Not call, No Movie.. always MainFragment")
                    }
                }
                Index.FRAGMENT_RECENTCALL   -> { //현재화면 최근응대고객 화면
                    replaceFragment(Index.FRAGMENT_MAIN)
                }
                Index.FRAGMENT_MOVIE        -> { //현재화면 동영상화면
                    if(ScreenInfo.usePlaySub) replaceFragment(Index.FRAGMENT_RECENTCALL)
                    else replaceFragment(Index.FRAGMENT_MAIN)
                }
                else -> { //현재화면 INIT, SETTING, 기타등등, 아무처리 없이 해당 화면에 그대로 있는다.
                    Log.d("ChangeFragment - 현재 화면 : ${getTagName(currentFragmentIndex)}")
                }
            }

            handlerChange.postDelayed(this, getDelayTime())
        }
    }
}