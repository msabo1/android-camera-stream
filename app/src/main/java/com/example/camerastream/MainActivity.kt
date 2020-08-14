package com.example.camerastream

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cameraIdMap = mapOf(backCameraRadioButton.id to 0, 0 to backCameraRadioButton.id, frontCameraRadioButton.id to 1, 1 to frontCameraRadioButton.id)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)).get<CreateStreamViewModel>(CreateStreamViewModel::class.java)

        streamUrlEditText.setText(viewModel.streamUrl)

        if(viewModel.camera != null && cameraIdMap[viewModel.camera!!] != null){
            cameraRadioGroup.check(cameraIdMap[viewModel.camera!!]!!)
        }

        frontCameraRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.camera = cameraIdMap[frontCameraRadioButton.id]
                backCameraRadioButton.isChecked = false
            }
        }
        backCameraRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.camera = cameraIdMap[backCameraRadioButton.id]
                frontCameraRadioButton.isChecked = false
            }
        }

        val textAdapter: TextAdapter = TextAdapter(ArrayList(), TextsLiveData::setTextLabel, TextsLiveData::setTextText, TextsLiveData::setTextPositionX, TextsLiveData::setTextPositionY, TextsLiveData::deleteText)
        textList.adapter = textAdapter
        textList.layoutManager = LinearLayoutManager(this)

        TextsLiveData.texts.observe(this, Observer { texts ->
            textAdapter.loadNewData(texts)
            textList.scrollToPosition(texts.size - 1)
        })

        addTextButton.setOnClickListener{
            viewModel.addText()
        }

        streamUrlEditText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                viewModel.streamUrl = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        startStreamingButton.setOnClickListener{
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.CAMERA), 1);

            viewModel.startStream()

            val intent = Intent(this, StreamActivity::class.java)
            startActivity(intent)

        }
    }
}