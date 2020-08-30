package com.example.camerastream

import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.extractor.ts.TsExtractor
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.UdpDataSource
import com.google.android.exoplayer2.util.TimestampAdjuster
import kotlinx.android.synthetic.main.activity_stream.*


class StreamActivity : AppCompatActivity() {
    var player: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream)

        val progressBarView: View = layoutInflater.inflate(R.layout.progress_bar, null)
        streamActivity.addView(progressBarView)

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

        val trackSelector = DefaultTrackSelector(this, AdaptiveTrackSelection.Factory());
        val simpleExoPlayerBuilder: SimpleExoPlayer.Builder = SimpleExoPlayer.Builder(this)
        simpleExoPlayerBuilder.setTrackSelector(trackSelector)

        player = simpleExoPlayerBuilder.build()

        playerView.player = player
        playerView.requestFocus()
        val factory: DataSource.Factory = DataSource.Factory({ UdpDataSource(3000, 100000) })
        val tsExtractorFactory = ExtractorsFactory {
            arrayOf<TsExtractor>(
                TsExtractor(
                    TsExtractor.MODE_SINGLE_PMT,
                    TimestampAdjuster(0), DefaultTsPayloadReaderFactory()
                )
            )
        }
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(factory, tsExtractorFactory).createMediaSource(Uri.parse("udp://127.0.0.1:1234"))
        player?.prepare(mediaSource)
        player?.playWhenReady = true

        player?.addListener(object: EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if(playbackState == STATE_READY){
                    playerView.visibility = View.VISIBLE
                    streamActivity.removeView(progressBarView)
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer(){
        player?.playWhenReady = false
        player?.stop()
        player?.release()
        player = null
    }
}