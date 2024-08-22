package com.kct.iqsdisplayer.ui

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfoManager
import com.kct.iqsdisplayer.databinding.FragmentMovieBinding
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.getFileExtension
import java.util.Locale

class FragmentMovie : Fragment() {
    private var _binding: FragmentMovieBinding? = null
    private val binding get() = _binding!!

    private val mp: MediaPlayer = MediaPlayer() //mp가 null인 경우는 없다.

    //플레이 리스트
    private var currentIndex = 0 //현재 플레이리스트 인덱스
    private val list: ArrayList<String> = ArrayList()

    //이미지 표출 변수 및 객체
    private var msMaxPlayTime = 0 //이미지,영상 최대 표출시간
    private var playTimerSec = 0 //이미지,영상 표출한 시간
    private var playedPosition = 0 //영상이 재생된 위치, 중간에 끊겼을 때 이어 재생하기용

    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var runShowMedia: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        handler.removeCallbacks(runShowMedia) // 핸들러 콜백 제거
        mp.stop()
        mp.release()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val surfaceHolder: SurfaceHolder = binding.sfVideo.holder
        surfaceHolder.addCallback(sfCallback)

        makeList()

        initVideo()

        initHandler()
    }


    private val sfCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mp.setDisplay(holder)
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {}
    }

    //================================================================================================================================================================
    // 컨텐츠 재생 관련 코드
    //================================================================================================================================================================
    private fun makeList() {
        list.clear()

        val fileNames = ScreenInfoManager.instance.adFileList
        for (fileName in fileNames) {
            if (fileName.isEmpty()) continue

            val path = Const.Path.DIR_VIDEO + fileName
            list.add(path)
        }
    }

    private fun initHandler() {
        runShowMedia = Runnable {
            playTimerSec++ //postDelayed로 호출 했으므로 1초 증가
            val isImage = isImage(list[currentIndex])
            val logMsg = if (isImage) "이미지 재생 중 - " else "영상 재생 중 - "
            Log.v("$logMsg$playTimerSec 초 경과")

            val maxPlayTimeSec = msMaxPlayTime / 1000
            if (playTimerSec >= maxPlayTimeSec) { //타임아웃
                if (isImage) {
                    //이미지는 10초 보여줬으면 다음 컨텐츠로 변경, 영상은 Complete 될 때까지 Index 변경 없음.
                    changeNextIndex()
                }
                resetPlayTimer()
                //(activity as MainActivity?).restart(0)
            } else {
                runPlayTimer()
            }
        }
    }

    private fun runPlayTimer() {
        handler.postDelayed(runShowMedia, 1000)
    }

    private fun stopPlayTimer() {
        handler.removeCallbacks(runShowMedia)
    }

    private fun resetPlayTimer() {
        playTimerSec = 0
    }

    fun playMedia() {
        if (list.isEmpty()) return

        val contentsPath = list[currentIndex]
        when {
            isImage(contentsPath) -> {
                binding.ivImage.visibility = View.VISIBLE
                setImageView(contentsPath)
            }
            else -> {
                binding.ivImage.visibility = View.INVISIBLE
                startVideo(contentsPath)
            }
        }
        handler.postDelayed(runShowMedia, 1000)
    }

    /**
     * OnCompleteListener 가 아닌 수동으로 멈출 때 호출 됨
     */
    fun stopMedia() {
        if (mp.isPlaying) {
            mp.pause()
            playedPosition = mp.currentPosition
            Log.d("재생 일시정지 Index($currentIndex) : ${list[currentIndex]}, 재생위치 저장: $playedPosition mSec")
        } else {
            Log.d("재생 중지 된 Index($currentIndex) : ${list[currentIndex]}")
        }

        val isImage = isImage(list[currentIndex])
        if (!isImage) playTimerSec = 0

        handler.removeCallbacks(runShowMedia)
    }

    private fun skipOnErrorAndContinuePlay() {
        stopMedia()

        playNext()
    }

    /**
     * 주로 영상에 error 가 있거나 할 때 쓴다.
     * 보통은 index가 변경되고 바로 영상 재생 할 이유가 없다.
     */
    private fun playNext() {
        Log.w("재생 완료 된 Index($currentIndex) : ${list[currentIndex]}")
        playedPosition = 0
        currentIndex = changeNextIndex()
        playMedia()
    }

    private fun changeNextIndex(): Int {
        currentIndex++
        if (currentIndex >= list.size) {
            currentIndex = 0
        }
        Log.d("다음 재생 Index($currentIndex) : ${list[currentIndex]}")
        return currentIndex
    }

    fun addList(fileName: String?) {
        if (!fileName.isNullOrEmpty()) {
            val sb = Const.Path.DIR_VIDEO + fileName
            list.add(sb)
        }
    }

    fun removeListAll() {
        list.clear()
    }

    /**
     * 미디어 표출시간 설정, 내부적으로 millisecond로  변경하여 사용한다.
     * @param mSecPlayTime 10000이 들어오는데 10초를 의미한다.
     */
    fun setMSecPlayTime(mSecPlayTime: Int) {
        this.msMaxPlayTime = mSecPlayTime
    }

    //================================================================================================================================================================
    // 영상관련 코드
    //================================================================================================================================================================
    private fun initVideo() {
        mp.setVolume(0f, 0f) //볼륨 제거
        mp.setOnPreparedListener(preparedListener)
        mp.setOnCompletionListener(completeListener)
        mp.setOnErrorListener(errorListener)
    }

    private fun resetVideo() {
        playedPosition = 0
        mp.stop()
        mp.reset()
    }

    private fun startVideo(path: String) {
        if (playedPosition > 0) {
            mp.seekTo(playedPosition)
            mp.start()
            Log.d("영상 이어서 시작 Index($currentIndex) : ${list[currentIndex]}, $playedPosition mSec 부터 재생 시작")
        } else {
            try {
                mp.setDataSource(path)
                mp.prepareAsync()
                Log.d("영상 처음부터 시작 Index($currentIndex) : ${list[currentIndex]}")
            } catch (e: Exception) {
                Log.i("video play error__${path}__(${e.message}) $currentIndex/${list.size}")
                //동영상 에러 시 다음 컨텐츠로..
                skipOnErrorAndContinuePlay()
            }
        }
    }

    private val preparedListener = MediaPlayer.OnPreparedListener { it.start() }

    private val completeListener = MediaPlayer.OnCompletionListener {
        Log.i("재생 완료 된 Index($currentIndex) : ${list[currentIndex]}")
        changeNextIndex()

        stopPlayTimer()
        resetPlayTimer()
        resetVideo()
        // TODO: (activity as MainActivity?)?.restart(0) 필요한 경우 처리
    }

    private val errorListener = MediaPlayer.OnErrorListener { _: MediaPlayer?, what: Int, extra: Int ->
        val contentsPath: String = list[currentIndex]
        Log.e("재생 실패 된 Index($currentIndex) : $contentsPath what:$what extra:$extra")

        skipOnErrorAndContinuePlay()
        true
    }



    //================================================================================================================================================================
    // 이미지 관련 코드
    //================================================================================================================================================================
    private fun setImageView(strImagePath: String?) {
        Glide.with(this)
            .asBitmap()
            .load(strImagePath)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .override(1200, 800)
            )
            .listener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                    Log.i("Glide load failed: ")
                    e?.message
                    skipOnErrorAndContinuePlay()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivImage.setImageBitmap(resource)
                    return false
                }
            })
            .into(binding.ivImage)
    }

    private fun isImage(contentsPath: String): Boolean {
        val ext = contentsPath.getFileExtension().lowercase(Locale.getDefault())
        return ext == "jpg" || ext == "jpeg" || ext == "png"
    }

}