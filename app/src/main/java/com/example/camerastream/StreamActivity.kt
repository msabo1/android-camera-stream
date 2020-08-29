package com.example.camerastream

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_stream.*


class StreamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream)

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get<StreamViewModel>(
            StreamViewModel::class.java
        )

        ExecutionData.isTerminated.observe(this, Observer { isTerminated ->
            if(isTerminated){
                Toast.makeText(this, "Stream could not be started!", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        val updateTextAdapter: UpdateTextAdapter = UpdateTextAdapter(ArrayList(), viewModel::updateText)
        updateTextList.adapter = updateTextAdapter
        updateTextList.layoutManager = LinearLayoutManager(this)

        TextsLiveData.texts.observe(this, Observer { texts ->
            updateTextAdapter.loadNewData(texts)
        })

        stopStreamingButton.setOnClickListener {
            viewModel.stopStreaming()
            finish()
        }
    }
}