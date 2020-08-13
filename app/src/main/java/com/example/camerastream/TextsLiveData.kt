package com.example.camerastream

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object TextsLiveData {
    private val textsMutable: MutableLiveData<ArrayList<Text>> = MutableLiveData<ArrayList<Text>>()
    val texts: LiveData<ArrayList<Text>>
        get() = textsMutable
    val textsArrayList: ArrayList<Text>?
        get() = texts.value

    fun addText(text: Text){
        if(textsMutable.value != null){
            textsMutable.value?.add(text);
            textsMutable.value = textsMutable.value
        }else{
            textsMutable.value = arrayListOf(text)
        }

    }

    fun setTextLabel(position: Int, label: String){
        if(textsMutable.value?.get(position) != null){
            textsMutable.value?.get(position)?.label = label
        }
    }

    fun setTextText(position: Int, text: String){
        if(textsMutable.value?.get(position) != null){
            textsMutable.value?.get(position)?.text = text
        }
    }

    fun setTextPositionX(position: Int, positionX: String){
        if(textsMutable.value?.get(position) != null){
            textsMutable.value?.get(position)?.positionX = positionX.toFloat()
        }
    }

    fun setTextPositionY(position: Int, positionY: String){
        if(textsMutable.value?.get(position) != null){
            textsMutable.value?.get(position)?.positionY = positionY.toFloat()
        }
    }


}