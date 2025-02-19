package com.example.urbango.screens

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.urbango.viewModels.PermissionViewModel
import java.io.File

@Composable
fun CameraScreen(viewModel: PermissionViewModel = viewModel()) {
    val context = LocalContext.current
    val hasCameraPermission by viewModel.hasCameraPermission.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    val videoUri by viewModel.videoUri.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateCameraPermission(isGranted)
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { viewModel.savePhoto(it, context) }
    }

    val videoFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "video_${System.currentTimeMillis()}.mp4")
    val videoUriForCapture = Uri.fromFile(videoFile)

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) viewModel.saveVideo(videoUriForCapture)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    CameraScreenUI(
        hasCameraPermission = hasCameraPermission,
        photoUri = photoUri,
        videoUri = videoUri,
        onTakePhoto = { photoLauncher.launch() },
        onRecordVideo = { videoLauncher.launch(videoUriForCapture) }
    )
}

@Composable
fun CameraScreenUI(
    hasCameraPermission: Boolean,
    photoUri: Uri?,
    videoUri: Uri?,
    onTakePhoto: () -> Unit,
    onRecordVideo: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasCameraPermission) {
            Button(onClick = onTakePhoto) { Text("Take Photo") }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRecordVideo) { Text("Record Video") }
            Spacer(modifier = Modifier.height(16.dp))

            photoUri?.let {
                Text("Photo saved at: $it")
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.size(200.dp)
                )
            }

            videoUri?.let { Text("Video saved at: $it") }
        } else {
            Text("Camera permission is required to use this feature.")
        }
    }
}

