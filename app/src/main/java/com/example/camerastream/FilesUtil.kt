package com.example.camerastream

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FilesUtil {
    companion object{
        fun rawResourceToFile(context: Context, resource: Int, file: File){
            val inputStream: InputStream = context.resources.openRawResource(resource)
            if (file.exists()) {
                file.delete()
            }
            val outputStream = FileOutputStream(file)

            try {
                val buffer = ByteArray(1024)
                var readSize: Int
                while (inputStream.read(buffer).also{ readSize = it } > 0) {
                    outputStream.write(buffer, 0, readSize)
                }
            } catch (e: IOException) {
                Log.e("Saving raw resource", "Saving raw resource failed")
            } finally {
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            }
        }

        fun stringToFile(string: String, file: File){
            Log.i("mojtag", string)
            Log.i("mojtag", file.toString())
            val outputStream = FileOutputStream(file)
            try {
                outputStream.write(string.toByteArray())
            }catch (e: IOException){
                Log.e("Saving string", "Saving string to file failed")
            }finally {
                outputStream.close()
            }
        }
    }

}