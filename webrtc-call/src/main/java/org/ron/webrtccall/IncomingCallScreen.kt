/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Incoming Call Screen Composable
 */
package org.ron.webrtccall

import android.media.RingtoneManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IncomingCallComposable(
    callerName: String,
    isAudioOnly: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current
    val ringtone = remember {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        RingtoneManager.getRingtone(context, uri)
    }

    DisposableEffect(Unit) {
        ringtone.play()
        onDispose { ringtone.stop() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A1A), Color(0xFF000000))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Image / Avatar with Pulse
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = callerName,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            
            Text(
                text = if (isAudioOnly) "Incoming Voice Call..." else "Incoming Video Call...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(64.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Decline Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LargeCallButton(
                        onClick = onDecline,
                        icon = Icons.Default.CallEnd,
                        color = Color(0xFFE53935),
                        description = "Decline"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Decline", color = Color.White)
                }

                // Answer Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LargeCallButton(
                        onClick = onAnswer,
                        icon = Icons.Default.Call,
                        color = Color(0xFF43A047),
                        description = "Answer"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Answer", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LargeCallButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    description: String
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(84.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = color)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(42.dp),
            tint = Color.White
        )
    }
}
