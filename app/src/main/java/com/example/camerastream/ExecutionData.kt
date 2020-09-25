package com.example.camerastream

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object ExecutionData {
    var executionId: Long? = null
    private val isTerminatedMutable: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isTerminated: LiveData<Boolean>
        get() = isTerminatedMutable

    fun setIsTerminated(terminated: Boolean){
        isTerminatedMutable.value = terminated
    }
}