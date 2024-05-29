package com.irempamukcu.deteppproject

import CameraSource
import android.Manifest
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.irempamukcu.deteppproject.databinding.FragmentVideoBinding
import kotlinx.coroutines.launch
import java.io.IOException

class Video : Fragment() {
    private lateinit var binding: FragmentVideoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var videoUrl = ""
    private var kidName = ""
    private var kidColor = ""
    private var isPlaying = false
    private var permissionGranted = true
    private var kidPermission = ""
    private var happyCount = 0
    private var sadCount = 0
   // private lateinit var cameraSource : CameraSource

    //bura düşünülecek kafam karıştı


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("VideoFragment", "onCreate called")
    }

    override fun onPause() {
        super.onPause()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        //cameraSource.stop()
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("VideoFragment", "onCreateView called")
        arguments?.let {
            videoUrl = VideoArgs.fromBundle(it).videoUrl
            kidName = VideoArgs.fromBundle(it).kidName
            kidColor = VideoArgs.fromBundle(it).kidColor
             // Check permission argument
        }

       // cameraSource = CameraSource(requireContext())
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        //cameraSource.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
       // cameraSource.stop()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VideoFragment", "onViewCreated called")

        val videoView = binding.currentVideoVideo
        val backButton = binding.imageBackVideo
        val notifyButton = binding.imageNotifyVideo

        //loadKidPermission()

        checkPermission(kidPermission)


        loadVideo(videoUrl)

        if (permissionGranted) {
            //setupCamera()
        }

        videoView?.setOnClickListener {
            toggleControlsVisibility()
        }

        backButton?.setOnClickListener {
            val action = VideoDirections.actionVideoToLoggedIn(kidName, kidColor)
            findNavController().navigate(action)
        }

        notifyButton?.setOnClickListener {
            // notify function
        }
    }

    private fun loadVideo(videoUrl: String) {
        Log.d("VideoFragment", "Loading video from URL: $videoUrl")
        val storageReference = FirebaseStorage.getInstance().getReference("videos/$videoUrl")

        storageReference.downloadUrl.addOnSuccessListener { uri ->
            if (isAdded) {
                Log.d("VideoFragment", "Video URI: $uri")
                playVideo(uri)
            }
        }.addOnFailureListener { exception ->
            if (isAdded) {
                Log.e("VideoFragment", "Failed to load video", exception)
                Toast.makeText(requireContext(), "Video yükleme başarısız.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun playVideo(uri: Uri) {
        if (!isAdded) return
        Log.d("VideoFragment", "Playing video from URI: $uri")
        val videoView = binding.currentVideoVideo
        if (videoView != null) {
            val mediaController = MediaController(context)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(uri)
            videoView.requestFocus()
            videoView.setZOrderOnTop(true)
            videoView.setOnPreparedListener {
                it.start()
                isPlaying = true
                Log.d("VideoFragment", "Video started playing")
            }
            videoView.setOnCompletionListener {
                isPlaying = false
                Log.d("VideoFragment", "Video playback completed")
                //printEmotionResults()
            }
        } else {
            Log.e("VideoFragment", "VideoView is null")
        }
    }

    private fun toggleControlsVisibility() {
        val backButton = binding.imageBackVideo
        val notifyButton = binding.imageNotifyVideo

        backButton.visibility = if (backButton.visibility == View.GONE) View.VISIBLE else View.GONE
        notifyButton.visibility = if (notifyButton.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    /*
        private fun setupCamera() {
            val surfaceView = SurfaceView(requireContext())
            binding.root.addView(surfaceView)

            val surfaceHolder = surfaceView.holder
            surfaceHolder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    startCamera(holder)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }
            })
        }

        private fun startCamera(holder: SurfaceHolder) {

            cameraSource.start(holder)

            cameraSource.setFrameProcessor { frame ->
                processFrame(frame)
            }
        }

        private fun processFrame(frame: InputImage) {
            val realTimeOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()

            val detector = FaceDetection.getClient(realTimeOpts)

            detector.process(frame)
                .addOnSuccessListener { faces ->
                    for (face in faces) {
                        analyzeFace(face)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("VideoFragment", "Face detection failed", e)
                }
        }

        private fun analyzeFace(face: Face) {
            val smileProb = face.smilingProbability ?: 0.0f
            val rightEyeOpenProb = face.rightEyeOpenProbability ?: 0.0f
            val leftEyeOpenProb = face.leftEyeOpenProbability ?: 0.0f

            // A basic heuristic for detecting happiness and sadness
            val detectedEmotion = when {
                smileProb > 0.3 && rightEyeOpenProb > 0.3 && leftEyeOpenProb > 0.3 -> "happy"
                smileProb < 0.3 -> "sad"
                else -> "neutral"
            }

            when (detectedEmotion) {
                "happy" -> happyCount++
                "sad" -> sadCount++
            }

            Log.d("VideoFragment", "ton = binding.imageBackVideo
            val notifyButton = binding.imageNotifyVideo

            backButton.visibility = if (backButton.visibility == View.GONE) View.VISIBLE else View.GONE
            notifyButton.visibility = if (notifyButton.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        private fun loadKidPermission(){
            val currentUser = auth.currentUser
            val currentMail = currentUser?.email

            firestore.collection("kids").whereEqualTo("mail",currentMail).whereEqualTo("kidName",kidName)
                .get().addOnSuccessListener {documents->
                    for(document in documents){
                        kidPermission = document.getString("kidPermission") ?: ""
                    }

                }
        }
    */


    private fun checkPermission(kidPermission : String){
        if(kidPermission.equals("yes")){
            permissionGranted = true
        }else if(kidPermission.equals("no")){
            permissionGranted = false
        }
    }
}
