package com.example.camerastream

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val texts: ArrayList<Text> = ArrayList()
        val text = Text()
        text.label = "Mario"
        texts.add(text)


        val textAdapter: TextAdapter = TextAdapter(texts)
        textList.adapter = textAdapter
        textList.layoutManager = LinearLayoutManager(this)

        addTextButton.setOnClickListener{
            texts.add(Text())
            textAdapter.loadNewData(texts)
            textList.scrollToPosition(texts.size - 1)
        }
    }
}