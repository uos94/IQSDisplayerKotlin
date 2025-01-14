package com.kct.iqsdisplayer.ui

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
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
import com.kct.iqsdisplayer.R
import com.kct.iqsdisplayer.common.Const
import com.kct.iqsdisplayer.common.ScreenInfo
import com.kct.iqsdisplayer.databinding.FragmentMovieBinding
import com.kct.iqsdisplayer.ui.FragmentFactory.Index
import com.kct.iqsdisplayer.ui.FragmentFactory.replaceFragment
import com.kct.iqsdisplayer.util.Log
import com.kct.iqsdisplayer.util.getFileExtension
import java.util.Locale

class FragmentMovie : Fragment() {
    private var _binding: FragmentMovieBinding? = null
    private val binding get() = _binding!!

    private var mp: MediaPlayer = MediaPlayer()

    private var currentIndex = 0 //현재 플레이리스트 인덱스
    private val list: ArrayList<String> = ArrayList()

    private var playedPosition = 0 //영상이 재생된 위치, 중간에 끊겼을 때 이어 재생하기용

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)

        val surfaceHolder: SurfaceHolder = binding.sfVideo.holder
        surfaceHolder.addCallback(sfCallback)

        setUI()

        initVideo()

        makeList()

        return binding.root
    }

    private fun setUI() {
        binding.tvWinNum.text = ScreenInfo.winNum.toString()

        ScreenInfo.waitNum.observe(viewLifecycleOwner) {
            binding.tvWaitNum.text  = it.toString()
        }

        ScreenInfo.normalCallData.observe(viewLifecycleOwner) {
            binding.tvCallNum.text  = if(it.callNum == 0) "" else getString(R.string.format_four_digit).format(it.callNum)
        }
    }

    override fun onPause() {
        super.onPause()
        stopMedia()
    }

    private val sfCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mp.setDisplay(holder)

            playMedia()
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            mp.setDisplay(null)

            holder.removeCallback(this)
        }
    }

    private fun makeList() {
        list.clear()

        val fileNames = ScreenInfo.mediaFileNameList
        list.addAll(fileNames.map { Const.Path.DIR_VIDEO + it })
    }

    private fun playMedia() {
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
    }

    /**
     * OnCompleteListener 가 아닌 수동으로 멈출 때 호출 됨
     */
    private fun stopMedia() {
        if (list.isEmpty()) return

        val currentContentPath = list[currentIndex]
        val isImage = isImage(currentContentPath)
        if(isImage) {
            changeNextIndex()
        }
        else {
            if (mp.isPlaying) pauseVideo()
            else stopVideo()
        }
    }

    private fun skipOnErrorAndContinuePlay() {
        stopVideo()

        playNext()
    }

    /**
     * 주로 영상에 error 가 있거나 할 때 쓴다.
     * 보통은 index가 변경되고 바로 영상 재생 할 이유가 없다.
     */
    private fun playNext() {
        playedPosition = 0

        changeNextIndex()

        playMedia()
    }

    private fun changeNextIndex(): Int {
        currentIndex++
        if (currentIndex >= list.size) currentIndex = 0

        playedPosition = 0

        return currentIndex
    }

    private fun initVideo() {
        mp = MediaPlayer()
        mp.setVolume(0f, 0f) //볼륨 제거
        mp.setOnPreparedListener(preparedListener)
        mp.setOnCompletionListener(completeListener)
        mp.setOnErrorListener(errorListener)
    }

    private fun pauseVideo() {
        playedPosition = mp.currentPosition
        mp.stop()
        //Log.v("재생 일시정지 Index($currentIndex) : ${list[currentIndex]}, 재생위치 저장: $playedPosition mSec")
    }

    private fun stopVideo() {
        playedPosition = 0
        mp.stop()
    }

    private fun releaseVideo() {
        mp.reset()
        mp.release()
        mp = MediaPlayer()
    }


    private fun startVideo(path: String) {
        try {
            mp.setDataSource(path)
            mp.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("video play error__${path}__(${e.message}) $currentIndex/${list.size}")
            skipOnErrorAndContinuePlay()
        }
    }

    private val preparedListener = MediaPlayer.OnPreparedListener {
        if (playedPosition > 0) {   //이전에 보던 영상이 있으면 이어서 재생
            //Log.v("영상 이어서 시작 Index($currentIndex) : ${list[currentIndex]}, $playedPosition mSec 부터 재생 시작")
            mp.seekTo(playedPosition)
        }
        mp.start()
    }

    private val completeListener = MediaPlayer.OnCompletionListener {
        //Log.v("재생 완료 된 Index($currentIndex) : ${list[currentIndex]}")
        stopVideo()

        changeNextIndex()

        val isAvailableRecent   = ScreenInfo.usePlaySub && ScreenInfo.lastCallList.value!!.size > 0

        replaceFragment(if(isAvailableRecent)Index.FRAGMENT_RECENT_CALL else Index.FRAGMENT_MAIN)
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
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                    val contentsPath: String = list[currentIndex]
                    Log.e("재생 실패 된 Index($currentIndex) : $contentsPath errorMsg:${e?.message}")
                    skipOnErrorAndContinuePlay()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap?>?,
                    dataSource: DataSource,
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