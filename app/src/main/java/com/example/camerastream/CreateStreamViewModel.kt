package com.example.camerastream

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

class CreateStreamViewModel(application: Application): AndroidViewModel(application) {
    private val streamUrlMutable: MutableLiveData<String> = MutableLiveData<String>()
    val streamUrl: LiveData<String>
        get() = streamUrlMutable
    var camera: Int? = 0

    fun changeStreamUrl(url: String){
        streamUrlMutable.value = url;
    }

    fun addText(){
        val text: Text = Text(System.currentTimeMillis())
        TextsLiveData.addText(text)
    }

    fun startStream(){
        ExecutionData.setIsTerminated(false);
        createTextFiles()
        ExecutionData.executionId = FFmpeg.executeAsync(generateFFmpegCommand()) { executionId, rc ->
            if (rc == Config.RETURN_CODE_SUCCESS) {
                Log.i(Config.TAG, "Async command execution completed successfully.")
            } else if (rc == Config.RETURN_CODE_CANCEL) {
                Log.i(Config.TAG, "Async command execution cancelled by user.")
            } else {
                Log.i(Config.TAG, String.format("Async command execution failed with rc=%d.", rc))
                ExecutionData.setIsTerminated(true)
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
        command += "-f lavfi -i anullsrc "

        command += "-flags +global_header -c:v libx264 -c:a aac -b:v 1000k -maxrate 1000k -bufsize 2000k -g 50 "

        TextsLiveData.textsArrayList?.let { list ->
            command += "-vf \""
            for(text in list){
                val positionX = if(text.positionX != null) text.positionX!!/100 else 0
                val positionY = if(text.positionY != null) text.positionY!!/100 else 0
                command += "drawtext=fontfile="+ fontFile + ": fontsize=96: fontcolor=white: x=(w-text_w)*" + positionX + ": y=(h-text_h)*" + positionY + ": textfile=" + """${getApplication<Application>().filesDir}/${text.id}.txt""" +":reload=1, "
            }
            //Delete trail comma and space
            command = command.substring(0, command.length - 2)
            command += "\" "
        }

        command += "-s 258x458 -preset ultrafast -r 10 -f tee -map 0:v -map 1:a \"[f=flv]"
        command += streamUrl.value
        command += "|[f=mpegts]udp://127.0.0.1:1234\""

        Log.i("mojtag", command)
        return command
    }
}