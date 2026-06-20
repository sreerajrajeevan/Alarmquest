package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.viewmodel.AlarmViewModel
import com.example.viewmodel.VerificationUIState
import kotlinx.coroutines.delay
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun CameraVerificationScreen(
    viewModel: AlarmViewModel,
    alarmId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val verificationState by viewModel.verificationState.collectAsStateWithLifecycle()
    val targetObject by viewModel.targetChallengeObject.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        viewModel.resetVerificationUI()
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Scaffold(
        containerColor = NothingBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasCameraPermission) {
                // Permission request screen fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = "Permission Denied",
                        tint = NothingRed,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "CAMERA PERMISSION REQUIRED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AlarmQuest relies entirely on on-device machine learning to verify photos of physical objects. Please grant camera access in settings to complete challenges.",
                        fontSize = 13.sp,
                        color = NothingWhite.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    NothingButton(
                        text = "GRANT ACCESS",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            } else {
                // Main camera flows
                when (val state = verificationState) {
                    is VerificationUIState.Idle -> {
                        // 1. Camera Preview & Capture Layout
                        CameraPreviewLayout(
                            context = context,
                            executor = cameraExecutor,
                            onImageCaptureCreated = { imageCapture = it },
                            onCaptureClick = {
                                val capture = imageCapture
                                if (capture != null) {
                                    takePhotoInMemory(
                                        imageCapture = capture,
                                        executor = cameraExecutor,
                                        context = context,
                                        onPhotoCaptured = { bitmap ->
                                            viewModel.verifyCapturedImage(bitmap)
                                        },
                                        onCaptureError = { exception ->
                                            Log.e("CameraScreen", "Capture execution error", exception)
                                        }
                                    )
                                }
                            },
                            targetObject = targetObject
                        )
                    }
                    is VerificationUIState.Processing -> {
                        // 2. Analytical Loader overlay
                        ProcessingLayout()
                    }
                    is VerificationUIState.Success -> {
                        // 3. Success splash celebration
                        SuccessLayout(onNavigateBack = onNavigateBack)
                    }
                    is VerificationUIState.Failure -> {
                        // 4. Failure instructions with retry CTA
                        FailureLayout(
                            error = state.error,
                            onRetry = { viewModel.resetVerificationUI() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewLayout(
    context: Context,
    executor: Executor,
    onImageCaptureCreated: (ImageCapture) -> Unit,
    onCaptureClick: () -> Unit,
    targetObject: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderProvider = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderProvider.get()

        val preview = Preview.Builder().build()
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        onImageCaptureCreated(imageCapture)

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Failed to bind CameraX use cases", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Texture Surface
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // HUD grid borders aesthetic (Nothing OS vibe)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(24.dp, NothingBlack)
        )

        // Overlay target focus box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(2.dp, NothingWhite.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(30.dp)) {
                // Focus indicator reticle
                drawCircle(color = NothingWhite.copy(alpha = 0.3f), radius = 15f)
            }
        }

        // Top information bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(NothingBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .border(1.dp, NothingGreyMedium, RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "TARGET: ${targetObject.uppercase()}",
                    color = NothingWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }

        // Bottom Capture shutter controller
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ALIGN OBJECT IN RETICLE & CAPTURE",
                fontSize = 11.sp,
                color = NothingWhite.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic White shutter button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(NothingWhite)
                    .clickable { onCaptureClick() }
                    .testTag("shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Capture",
                    tint = NothingBlack,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun ProcessingLayout() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing_spin")
    val dotAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticking"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dot progress counts
        DottedIndicator(
            activeCount = dotAnimation.value.toInt(),
            totalCount = 6,
            modifier = Modifier.scale(2f)
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "EXTRACTING VECTOR MAP",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NothingWhite,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Analyzing on-device contours & contrast grids...\nRunning ML Kit labeling...",
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = NothingWhite.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SuccessLayout(onNavigateBack: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Success",
            tint = NothingWhite,
            modifier = Modifier
                .size(80.dp)
                .testTag("success_checkmark")
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "CHALLENGE SOLVED",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NothingWhite,
            letterSpacing = 4.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Object verified at score >= 80%!\nAlarm deactivated. Statistics updated.",
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = NothingWhite.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(40.dp))
        DottedIndicator(activeCount = 4, totalCount = 4)
    }
}

@Composable
fun FailureLayout(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Failed",
            tint = NothingRed,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "VERIFICATION FAILED",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NothingRed,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message
        NothingCard(borderColor = NothingGreyMedium) {
            Text(
                text = error,
                fontSize = 14.sp,
                color = NothingWhite,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        NothingButton(
            text = "RETRY CAPTURE",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().testTag("retry_capture_btn")
        )
    }
}

// In-Memory capture utilizing OnImageCapturedCallback
private fun takePhotoInMemory(
    imageCapture: ImageCapture,
    executor: Executor,
    context: Context,
    onPhotoCaptured: (Bitmap) -> Unit,
    onCaptureError: (ImageCaptureException) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                try {
                    val buffer = imageProxy.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    
                    // Handle rotate correction
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val correctedBitmap = rotateBitmap(rawBitmap, rotationDegrees)
                    
                    onPhotoCaptured(correctedBitmap)
                } catch (e: Exception) {
                    Log.e("CameraProxy", "Failed parsing image bytes", e)
                } finally {
                    imageProxy.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onCaptureError(exception)
            }
        }
    )
}

// Rotate matrix helper
private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
