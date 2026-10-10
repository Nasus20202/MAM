package dev.nasuta.mam.lab2

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CameraFragment : Fragment() {
    private lateinit var previewView: PreviewView

    // Fill this entire fragment with camera preview.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        previewView = view.findViewById(R.id.view_finder)
        return view
    }

    // Start camera feed if possible. Ask for user permission if needed.
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            lifecycleScope.launch {
                startCamera()
            }
        } else {
            requestCameraPermissionLauncher.launch(CAMERA)
        }
    }

    // Request user permission for using the camera.
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Camera permission granted",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied",
                    Toast.LENGTH_SHORT).show()
            }
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Select back camera as a default.
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases, then rebind to camera.
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)
            } catch(exc: Exception) {
                Log.e(javaClass.simpleName, "Camera binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }
}