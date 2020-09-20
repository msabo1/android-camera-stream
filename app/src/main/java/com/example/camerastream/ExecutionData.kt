package com.example.camerastream

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object ExecutionData {
    var executionId: Long? = null
    private val isTerminatedMutable: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isTerminated: LiveData<Boolean>
        get() = isTerminatedMutable

    private val isStartedMutable: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isStarted: LiveData<Boolean>
        get() = isStartedMutable

    var camera: Int = 0


    fun setIsTerminated(terminated: Boolean){
        //isTerminatedMutable.value = terminated
    }

    fun postIsStarted(started: Boolean){
        isStartedMutable.postValue(started)
    }

    fun setIsStarted(started: Boolean){
        isStartedMutable.value = started
    }
}