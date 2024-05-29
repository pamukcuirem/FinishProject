import android.content.Context
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias FrameProcessor = (InputImage) -> Unit

class CameraSource(private val context: Context) {

    private lateinit var frameProcessor: FrameProcessor
    private lateinit var cameraExecutor: ExecutorService

    fun start(holder: SurfaceHolder) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, holder)
        }, ContextCompat.getMainExecutor(context))
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun bindPreview(cameraProvider: ProcessCameraProvider, holder: SurfaceHolder) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider { request ->
                    val surface = holder.surface
                    request.provideSurface(surface, ContextCompat.getMainExecutor(context)) { }
                }
            }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                frameProcessor(image)
            }
            imageProxy.close()
        }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (exc: Exception) {
            Log.e("CameraSource", "Use case binding failed", exc)
        }
    }

    fun setFrameProcessor(processor: FrameProcessor) {
        frameProcessor = processor
    }

    fun stop() {
        cameraExecutor.shutdown()
    }
}
