package com.kct.iqsdisplayer.ui

import androidx.annotation.IntDef
import androidx.fragment.app.Fragment

object FragmentFactory {

    private var currentIndex = Index.NONE

    private val fragmentInit        = FragmentInit()
    private val fragmentSetting     = FragmentSetting()
    private val fragmentMain        = FragmentMain()
    private val fragmentBackupCall  = FragmentBackupCall()
    private val fragmentMovie       = FragmentMovie()
    private val fragmentSubScreen   = FragmentSubScreen()
    private val fragmentRecentCall  = FragmentRecentCall()

    @Index
    fun getCurrentIndex() = currentIndex

    fun getCurrentFragment() = getFragment(getCurrentIndex())

    fun getFragment(@Index index: Int): Fragment {

        currentIndex = index

        return when (index) {
            Index.FRAGMENT_INIT         -> fragmentInit
            Index.FRAGMENT_SETTING      -> fragmentSetting
            Index.FRAGMENT_MAIN         -> fragmentMain
            Index.FRAGMENT_BACKUP_CALL  -> fragmentBackupCall
            Index.FRAGMENT_MOVIE        -> fragmentMovie
            Index.FRAGMENT_SUB_SCREEN   -> fragmentSubScreen
            Index.FRAGMENT_RECENTCALL   -> fragmentRecentCall
            else -> fragmentInit
        }
    }

    fun getTagName(@Index index: Int) = when (index) {
        Index.NONE                  -> "NONE"
        Index.FRAGMENT_INIT         -> "FragmentInit"
        Index.FRAGMENT_SETTING      -> "FragmentSetting"
        Index.FRAGMENT_MAIN         -> "FragmentMain"
        Index.FRAGMENT_BACKUP_CALL  -> "FragmentBackupCall"
        Index.FRAGMENT_MOVIE        -> "FragmentMovie"
        Index.FRAGMENT_SUB_SCREEN   -> "FragmentSubScreen"
        Index.FRAGMENT_RECENTCALL   -> "FragmentRecentCall"
        else                        -> "UNKNOWN"
    }

    @IntDef(
        Index.NONE,                 //최초실행 아직 Fragment없음
        Index.FRAGMENT_INIT,        //init
        Index.FRAGMENT_SETTING,     //setting
        Index.FRAGMENT_MAIN,        //메인
        Index.FRAGMENT_BACKUP_CALL, //백업호출
        Index.FRAGMENT_MOVIE,       //동영상표출
        Index.FRAGMENT_SUB_SCREEN,  //보조표시기
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
            const val FRAGMENT_SUB_SCREEN: Int  = FRAGMENT_MOVIE + 1
            const val FRAGMENT_RECENTCALL: Int  = FRAGMENT_SUB_SCREEN + 1
        }
    }
}