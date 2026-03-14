package com.optimeter.app.presentation.scan

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.optimeter.app.domain.model.MeterType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.optimeter.app.R
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    meterType: MeterType,
    onNavigateBack: () -> Unit,
    onNavigateToValidation: (String, String) -> Unit, // Digits, encoded PhotoPath
    onNavigateToManual: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(uiState.isStable) {
        if (uiState.isStable && !uiState.showValidation) {
            val photoFile = File(context.cacheDir, "meter_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        viewModel.onPhotoCaptured(photoFile.absolutePath)
                    }
                    override fun onError(exception: ImageCaptureException) {
                        // Handle capture error silently in UI, fallback is manual
                    }
                }
            )
        }
    }

    val cameraPermissionState = com.google.accompanist.permissions.rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (uiState.showValidation) {
        LaunchedEffect(Unit) {
            val encodedPath = Uri.encode(uiState.capturedPhotoPath ?: "")
            onNavigateToValidation(uiState.detectedDigits, encodedPath)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor, TextAnalyzer { digits ->
                                coroutineScope.launch(Dispatchers.Main) {
                                    viewModel.onDigitsDetected(digits)
                                }
                            })
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        // Ignore bind exceptions
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(120.dp)
                .align(Alignment.Center)
                .border(
                    width = 4.dp,
                    color = if (uiState.isStable) Color.Green else Color.White
                )
        )

        Button(
            onClick = onNavigateToManual,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text(stringResource(R.string.enter_manually))
        }
        
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.camera_permission_required))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(stringResource(R.string.grant_permission))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToManual) {
                    Text(stringResource(R.string.enter_manually_instead))
                }
            }
        }
    }
}
