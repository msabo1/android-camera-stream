package com.example.camerastream

import android.app.Application
import androidx.lifecycle.*
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

class StreamViewModel(application: Application): AndroidViewModel(application) {

    fun updateText(position: Int, text: String){
        TextsLiveData.setTextText(position, text)
        if(TextsLiveData.textsArrayList?.get(position)?.id != null){
            val file: File = File(getApplication<Application>().filesDir, """${TextsLiveData.textsArrayList?.get(position)?.id}.txt""")
            FilesUtil.stringToFile(text, file)
        }
    }

    fun stopStreaming(){
        if(ExecutionData.executionId != null){
            FFmpeg.cancel(ExecutionData.executionId!!)
        }

    }

}