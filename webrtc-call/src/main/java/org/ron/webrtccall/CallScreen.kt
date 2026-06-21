package org.ron.webrtccall

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.*
import kotlin.math.roundToInt

@Composable
fun CallScreen(
    localTrack: VideoTrack?,
    remoteTrack: VideoTrack?,
    eglContext: EglBase.Context?,
    callDuration: String,
    isAudioOnly: Boolean,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isVideoEnabled: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Remote Video (Background)
        if (!isAudioOnly && eglContext != null) {
            VideoRenderer(
                track = remoteTrack,
                eglContext = eglContext,
                modifier = Modifier.fillMaxSize(),
                isMirror = false
            )
            
            if (remoteTrack == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Voice Call Placeholder
        if (isAudioOnly) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.Gray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(80.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Voice Call", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text(callDuration, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Timer for Video Call (Top Center)
        if (!isAudioOnly) {
            Text(
                text = callDuration,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Local Video (PiP)
        if (!isAudioOnly && localTrack != null && isVideoEnabled && eglContext != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.DarkGray)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            ) {
                VideoRenderer(
                    track = localTrack,
                    eglContext = eglContext,
                    modifier = Modifier.fillMaxSize(),
                    isMirror = true
                )
            }
        }

        // Bottom Controls
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp).wrapContentWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
            shape = CircleShape
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CallControlIcon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, "Mute", if (isMuted) Color.Red else Color.White, onMuteToggle)
                if (!isAudioOnly) {
                    CallControlIcon(if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff, "Video", if (isVideoEnabled) Color.White else Color.Red, onVideoToggle)
                    CallControlIcon(Icons.Default.Cameraswitch, "Flip", Color.White, onSwitchCamera)
                }
                CallControlIcon(if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown, "Speaker", if (isSpeakerOn) Color.Green else Color.White, onSpeakerToggle)
                IconButton(onClick = onEndCall, modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.Red)) {
                    Icon(Icons.Default.CallEnd, "End", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun VideoRenderer(
    track: VideoTrack?,
    eglContext: EglBase.Context,
    modifier: Modifier = Modifier,
    isMirror: Boolean = false
) {
    val renderer = remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    
    // Manage sink with DisposableEffect to ensure it stays attached
    DisposableEffect(track) {
        val currentRenderer = renderer.value
        if (track != null && currentRenderer != null) {
            track.addSink(currentRenderer)
        }
        onDispose {
            if (track != null && currentRenderer != null) {
                try {
                    track.removeSink(currentRenderer)
                } catch (e: Exception) {}
            }
        }
    }

    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setEnableHardwareScaler(true)
                setMirror(isMirror)
                renderer.value = this
                // Initial sink attach if track is already present
                track?.addSink(this)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CallControlIcon(icon: ImageVector, desc: String, tint: Color, onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(icon, desc, tint = tint, modifier = Modifier.size(28.dp)) }
}
