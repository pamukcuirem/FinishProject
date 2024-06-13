package com.irempamukcu.deteppproject

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.irempamukcu.deteppproject.databinding.FragmentVideoBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Video : Fragment() {
    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var videoUrl = ""
    private var kidName = ""
    private var kidColor = ""
    private var isPlaying = false
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var permissionFromFirebase: String = ""
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onPause() {
        super.onPause()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        stopCamera()
    }

    override fun onResume() {
        super.onResume()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (videoUrl.isNotEmpty()) {
            loadVideo(videoUrl)
        }
        fetchKidPermission { permissionGranted ->
            if (permissionGranted && isAdded) {
                requestCameraPermission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_binding != null) {
            binding.currentVideoVideo.stopPlayback()
            binding.currentVideoVideo.suspend() // Optional: Release the MediaPlayer resource
        }
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("VideoFragment", "onCreateView called")
        _binding = FragmentVideoBinding.inflate(inflater, container, false)

        arguments?.let {
            videoUrl = VideoArgs.fromBundle(it).videoUrl
            kidName = VideoArgs.fromBundle(it).kidName
            kidColor = VideoArgs.fromBundle(it).kidColor
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VideoFragment", "onViewCreated called")

        val videoView = binding.currentVideoVideo
        val backButton = binding.imageBackVideo
        val notifyButton = binding.imageNotifyVideo

        videoView?.setOnClickListener {
            toggleControlsVisibility()
        }

        backButton?.setOnClickListener {
            val action = VideoDirections.actionVideoToLoggedIn(kidName, kidColor)
            findNavController().navigate(action)
        }

        notifyButton?.setOnClickListener {
            saveVideoUrl()
        }
    }

    private fun loadVideo(videoUrl: String) {
        Log.d("VideoFragment", "Loading video from URL: $videoUrl")
        val storageReference = storage.getReference("videos/$videoUrl")

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

    private fun fetchKidPermission(callback: (Boolean) -> Unit) {
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val phonePermission = sharedPreferences.getString("phonePermission", "no")

        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        firestore.collection("kids")
            .whereEqualTo("mail", currentMail)
            .whereEqualTo("kidName", kidName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    permissionFromFirebase = document.getString("kidPermission").toString()
                }
                callback(phonePermission == "yes" && permissionFromFirebase == "yes")
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Bir şeyler ters gitti", Toast.LENGTH_LONG).show()
                callback(false)
            }
    }

    private fun addEmotion(emotion: String) {
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        val emotionData = hashMapOf(
            "emotion" to emotion,
            "videoUrl" to videoUrl,
            "kidName" to kidName,
            "mail" to currentMail
        )

        firestore.collection("analyzes").document().set(emotionData).addOnSuccessListener {
            println("Success")
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Duygu kaydı başarısız.", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestCameraPermission() {
        if (isAdded) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            Log.e(TAG, "Fragment not attached, cannot request permissions")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        Log.d(TAG, "Starting camera")
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        detectFaces(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                Log.d(TAG, "Camera started successfully")
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCamera() {
        if (::cameraProviderFuture.isInitialized) {
            cameraProviderFuture.get().unbindAll()
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun detectFaces(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)
        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val smilingProbability = face.smilingProbability
                    if (smilingProbability != null) {
                        if (smilingProbability > 0.5) {
                            addEmotion("happy")
                        } else {
                            addEmotion("sad")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveVideoUrl() {
        val inputsCollection = firestore.collection("inputs")
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        val videoData = hashMapOf(
            "url" to videoUrl,
            "mail" to currentMail
        )

        inputsCollection.document().set(videoData).addOnSuccessListener {
            Toast.makeText(requireContext(), "Bu video ile ilgili bildiriminiz alındı.", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private const val TAG = "VideoFragment"
    }
}
