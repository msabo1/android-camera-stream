package com.example.camerastream
import android.content.Context
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_stream.*


class StreamActivity : AppCompatActivity() {
    private  lateinit var viewModel: StreamViewModel

    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    private lateinit var cameraDevice: CameraDevice
    private val deviceStateCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            if (camera != null){
                cameraDevice = camera
                previewSession()
            }
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }
        override fun onError(camera: CameraDevice, error: Int) {
        }

    }
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private val cameraManager by lazy {
        this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun previewSession() {
        val surface = previewView.holder.surface

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(
            listOf(surface),
            object: CameraCaptureSession.StateCallback(){
                override fun onConfigureFailed(session: CameraCaptureSession) {
                }
                override fun onConfigured(session: CameraCaptureSession) {
                    if (session != null) {
                        captureSession = session
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                        captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    }
                }
            }, null)

    }

    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camara preview").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
        }
    }

    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>) : T? {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw  IllegalArgumentException("Key not recognized")
        }
    }

    private fun cameraId(lens: Int) : String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }
        } catch (e: CameraAccessException) {
        }
        return deviceId[0]
    }

    private fun connectCamera() {
        val deviceId = cameraId(if(ExecutionData.camera == 0) CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT)
        try {
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
        } catch (e: InterruptedException) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream)

        //val progressBarView: View = layoutInflater.inflate(R.layout.progress_bar, null)
        //streamActivity.addView(progressBarView)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get<StreamViewModel>(
            StreamViewModel::class.java
        )

        viewModel.setIsFullscreen(false)

        viewModel.isFullscreen.observe(this, Observer {isFullscreen: Boolean ->
            if(isFullscreen){
                val params: ConstraintLayout.LayoutParams = previewView.layoutParams as ConstraintLayout.LayoutParams
                params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.topToBottom = ConstraintLayout.LayoutParams.UNSET
                params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                params.marginEnd = 0
                params.marginStart = 0
                params.topMargin = 0
                params.bottomMargin = 0
                previewView.requestLayout()

                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                supportActionBar?.hide()

                stopStreamingButton.visibility = View.GONE
                updateTextList.visibility = View.GONE
            }else{
                val params: ConstraintLayout.LayoutParams = previewView.layoutParams as ConstraintLayout.LayoutParams
                params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = updateTextList.id
                params.bottomToTop = stopStreamingButton.id
                params.marginEnd = 16
                params.marginStart = 16
                params.topMargin = 16
                params.bottomMargin = 16
                previewView.requestLayout()
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

        ExecutionData.isStarted.observe(this, Observer {
            if(it){
                startBackgroundThread()
                if(previewView.holder.surface != null){
                    connectCamera()
                }else{
                    previewView.holder.addCallback(object: SurfaceHolder.Callback{
                        override fun surfaceChanged(
                            holder: SurfaceHolder?,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {}
                        override fun surfaceDestroyed(holder: SurfaceHolder?) {}
                        override fun surfaceCreated(holder: SurfaceHolder?) {
                            connectCamera()
                        }
                    })
                }
            }
        })
    }

    override fun onDestroy() {
        closeCamera()
        stopBackgroundThread()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if(viewModel.isFullscreen.value != null && viewModel.isFullscreen.value!!){
            viewModel.setIsFullscreen(false)
        }else{
            viewModel.stopStreaming()
            super.onBackPressed()
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

}