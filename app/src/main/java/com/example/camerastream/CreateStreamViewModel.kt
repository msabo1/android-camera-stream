package com.example.camerastream

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

class CreateStreamViewModel(application: Application): AndroidViewModel(application) {
    var streamUrl: String? = null
    var camera: Int? = 0

    fun addText(){
        val text: Text = Text(System.currentTimeMillis())
        TextsLiveData.addText(text)
    }

    fun startStream(){
        createTextFiles()
        ExecutionData.executionId = FFmpeg.executeAsync(generateFFmpegCommand()) { executionId, rc ->
            if (rc == Config.RETURN_CODE_SUCCESS) {
                Log.i(Config.TAG, "Async command execution completed successfully.")
            } else if (rc == Config.RETURN_CODE_CANCEL) {
                Log.i(Config.TAG, "Async command execution cancelled by user.")
            } else {
                Log.i(Config.TAG, String.format("Async command execution failed with rc=%d.", rc))
            }
        }
    }

    private fun createTextFiles(){
        if(TextsLiveData.textsArrayList != null){
            for(text: Text in TextsLiveData.textsArrayList!!){
                val file: File = File(getApplication<Application>().filesDir, """${text.id}.txt""")
                if(text.text != null){
                    FilesUtil.stringToFile(text.text!!, file)
                }
            }
        }
    }

    private  fun generateFFmpegCommand(): String{
        val fontFile: File = File("""${getApplication<Application>().cacheDir}font.tff""")
        FilesUtil.rawResourceToFile(getApplication<Application>(), R.raw.doppioone_regular, fontFile)

        var command: String = "-video_size hd720 -f android_camera -camera_index " +  camera + " -i anything "

        //Add dummy audio track
        command += "-f lavfi -i anullsrc -r 10 -y -c:v libx264 -c:a mp3 "

        command += "-s 360x640 -bufsize 2048k -vb 400k -maxrate 800k -f flv "
        command += "-vf \""
        for(text in TextsLiveData.textsArrayList!!){
            val positionX = if(text.positionX != null) text.positionX!!/100 else 0
            val positionY = if(text.positionY != null) text.positionY!!/100 else 0
            command += "drawtext=fontfile="+ fontFile + ": fontsize=96: fontcolor=white: x=(w-text_w)*" + positionX + ": y=(h-text_h)*" + positionY + ": textfile=" + """${getApplication<Application>().filesDir}/${text.id}.txt""" +":reload=1, "
        }
        //Delete trail comma and space
        command = command.substring(0, command.length - 2)

        command += "\" "
        command += streamUrl

        Log.i("mojtag", command)
        return command
    }
}