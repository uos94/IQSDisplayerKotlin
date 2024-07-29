package com.kct.iqsdisplayer.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.LogFile
import com.kct.iqsdisplayer.util.makeDir
import com.kct.iqsdisplayer.util.setFullScreen
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        setFullScreen()

        startSystem()
    }

    private fun startSystem() {
        if(checkStorage()) {
            //Storage 를 사용 할 준비가 되었다면 FRAGMENT_INIT부터 시작한다.
            showFragment(FragmentFactory.Index.FRAGMENT_INIT)
        }
    }

    private fun checkStorage(): Boolean {
        Log.d("OS의 API_LEVEL[${Build.VERSION.SDK_INT}]" )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestAllFilesAccessPermission()
            return false
        }
        //isExternalStorageManager()를 통과하면 Root디렉토리를 사용 할 수 있으나 확인차 체크함.
        if(Environment.getExternalStorageDirectory()?.absolutePath == null){
            finishApp("외부 저장소 경로(Root)를 가져오는 데 실패했습니다.")
            return false
        }
        else { //권한이 있으므로 초기 경로 Setting
            Const.Path.Device.DIR_ROOT = Environment.getExternalStorageDirectory()!!.absolutePath
            Const.Path.Device.DIR_SHARED_PREFS = "${filesDir.absolutePath}/shared_prefs/"
        }

        if(makeDir(Const.Path.Device.DIR_IQS) == null) {
            finishApp("PATH[${Const.Path.Device.DIR_IQS}] 폴더 생성 실패")
            return false
        }
        return true
    }



    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestAllFilesAccessPermission() {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts("package", packageName, null)
            requestAllFilesAccessLauncher.launch(intent)
        }
    }

    private val requestAllFilesAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // 모든 파일에 대한 접근 권한이 부여됨
                startSystem()
            } else {
                // 모든 파일에 대한 접근 권한이 거부됨, 앱이 동작 할 수 없음. 앱종료.
                finishApp("모든 파일에 대한 접근 권한이 거부됨.")
            }
        }
    }

    private fun finishApp(message: String = "앱 종료 호출") {
        Log.e(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finishAffinity()
    }


    private fun showFragment(@FragmentFactory.Index index: Int) {
        val tagName = FragmentFactory.getTagName(index)
        val fragment = FragmentFactory.getFragment(index)
        val transaction = supportFragmentManager.beginTransaction()

        //Index가 NONE인 경우는 앱실행 시 최초일 때, 이때는 hide할 fragment가 없다.
        if(FragmentFactory.getCurrentIndex() != FragmentFactory.Index.NONE) {
            transaction.hide(FragmentFactory.getCurrentFragment())    
        }

        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.fragment_container, fragment, tagName)
        }

        transaction.commit()
        LogFile.write("화면 변경 : $tagName")
    }
}