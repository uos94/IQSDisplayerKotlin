package com.kct.iqsdisplayer.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class SystemReadyModel : ViewModel() {
    // 각 상태를 나타내는 MutableStateFlow 변수들
    private val _isConnect = MutableStateFlow(false)
    val isConnect: StateFlow<Boolean> get() = _isConnect

    private val _isAuthPacket = MutableStateFlow(false)
    val isAuthPacket: StateFlow<Boolean> get() = _isAuthPacket

    private val _isReservePacket = MutableStateFlow(false)
    val isReservePacket: StateFlow<Boolean> get() = _isReservePacket

    private val _isMediaPacket = MutableStateFlow(false)
    val isMediaPacket: StateFlow<Boolean> get() = _isMediaPacket

    private val _isWaitPacket = MutableStateFlow(false)
    val isWaitPacket: StateFlow<Boolean> get() = _isWaitPacket

    private val _isUploadLog = MutableStateFlow(false)
    val isUploadLog: StateFlow<Boolean> get() = _isUploadLog

    // 모든 상태를 결합하여 systemReady Flow 생성
    val systemReady: Flow<Boolean> = combine(
        _isConnect, _isAuthPacket, _isUploadLog
    ) { isConnect, isAuthPacket, isUploadLog ->
        isConnect && isAuthPacket && isUploadLog
    }

    // Flow를 LiveData로 변환하여 외부에서 관찰 가능하도록 설정
    val systemReadyLiveData: LiveData<Boolean> = systemReady.asLiveData(viewModelScope.coroutineContext)

    // 각 상태 변경 함수들
    fun setIsConnect(value: Boolean) {
        _isConnect.value = value
    }

    fun setIsAuthPacket(value: Boolean) {
        _isAuthPacket.value = value
    }

    fun setIsReservePacket(value: Boolean) {
        _isReservePacket.value = value
    }

    fun setIsMediaPacket(value: Boolean) {
        _isMediaPacket.value = value
    }

    fun setIsWaitPacket(value: Boolean) {
        _isWaitPacket.value = value
    }

    fun setIsUploadLog(value: Boolean) {
        _isUploadLog.value = value
    }
}