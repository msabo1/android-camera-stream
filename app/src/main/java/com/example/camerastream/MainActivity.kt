package com.example.camerastream

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)).get<CreateStreamViewModel>(CreateStreamViewModel::class.java)

        streamUrl.setText(viewModel.streamUrl)
        if(viewModel.camera != null){
            cameraSpinner.setSelection(viewModel.camera!!)
        }


        val cameraAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.camera_options, android.R.layout.simple_spinner_item)
        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cameraSpinner.adapter = cameraAdapter

        cameraSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.camera = 0
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.camera = position
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

        streamUrl.addTextChangedListener(object: TextWatcher{
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