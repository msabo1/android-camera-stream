package com.example.camerastream
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.extractor.ts.TsExtractor
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.UdpDataSource
import com.google.android.exoplayer2.util.TimestampAdjuster
import kotlinx.android.synthetic.main.activity_stream.*


class StreamActivity : AppCompatActivity() {
    private  lateinit var viewModel: StreamViewModel
    private lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream)

        val progressBarView: View = layoutInflater.inflate(R.layout.progress_bar, null)
        streamActivity.addView(progressBarView)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get<StreamViewModel>(
            StreamViewModel::class.java
        )

        viewModel.setIsFullscreen(false)

        viewModel.isFullscreen.observe(this, Observer {isFullscreen: Boolean ->
            if(isFullscreen){
                val params: ConstraintLayout.LayoutParams = playerView.layoutParams as ConstraintLayout.LayoutParams
                params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.topToBottom = ConstraintLayout.LayoutParams.UNSET
                params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginEnd = 0
                params.marginStart = 0
                params.topMargin = 0
                params.bottomMargin = 0
                playerView.requestLayout()

                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                supportActionBar?.hide()

                stopStreamingButton.visibility = View.GONE
                updateTextList.visibility = View.GONE
            }else{
                val params: ConstraintLayout.LayoutParams = playerView.layoutParams as ConstraintLayout.LayoutParams
                params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = updateTextList.id
                params.bottomToTop = stopStreamingButton.id
                params.marginEnd = 16
                params.marginStart = 16
                params.topMargin = 16
                params.bottomMargin = 16
                playerView.requestLayout()
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                supportActionBar?.show()

                stopStreamingButton.visibility = View.VISIBLE
                updateTextList.visibility = View.VISIBLE
            }
        })

        ExecutionData.isTerminated.observe(this, Observer { isTerminated ->
            if(isTerminated){
                Toast.makeText(this, "Stream could not be started! Check your stream URL and internet connection!", Toast.LENGTH_LONG).show()
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
        val loadControlBuilder: DefaultLoadControl.Builder = DefaultLoadControl.Builder()
        loadControlBuilder.setBufferDurationsMs(100, DefaultLoadControl.DEFAULT_MAX_BUFFER_MS, 100, 100)
        val simpleExoPlayerBuilder: SimpleExoPlayer.Builder = SimpleExoPlayer.Builder(this)
        simpleExoPlayerBuilder.setTrackSelector(trackSelector)
        simpleExoPlayerBuilder.setLoadControl(loadControlBuilder.createDefaultLoadControl())

        player = simpleExoPlayerBuilder.build()

        playerView.player = player
        playerView.requestFocus()
        val factory: DataSource.Factory = DataSource.Factory{ UdpDataSource(3000, 100000) }
        val tsExtractorFactory = ExtractorsFactory {
            arrayOf<TsExtractor>(
                TsExtractor(
                    TsExtractor.MODE_SINGLE_PMT,
                    TimestampAdjuster(0), DefaultTsPayloadReaderFactory()
                )
            )
        }
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(factory, tsExtractorFactory).createMediaSource(Uri.parse("udp://127.0.0.1:1234"))
        player.prepare(mediaSource)
        player.playWhenReady = true

        player.addListener(object: Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if(playbackState == STATE_READY){
                    playerView.visibility = View.VISIBLE
                    streamActivity.removeView(progressBarView)
                }
            }
        })

        playerView.videoSurfaceView?.setOnClickListener{
            viewModel.setIsFullscreen(true)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onBackPressed() {
        if(viewModel.isFullscreen.value != null && viewModel.isFullscreen.value!!){
            viewModel.setIsFullscreen(false)
        }else{
            viewModel.stopStreaming()
            super.onBackPressed()
        }
    }

    private fun releasePlayer(){
        player.playWhenReady = false
        player.stop()
        player.release()
    }
}