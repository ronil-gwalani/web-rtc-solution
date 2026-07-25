/**
 * Created by Ronil Gwalani
 * WebRTC Solution - Home Screen
 */
package org.ron.webRtcSolution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val userId =""//by viewModel.userId.collectAsState()
    val userName =""//by viewModel.userName.collectAsState()


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Profile Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName?.take(1)?.uppercase() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Hello, $userName",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "ID: $userId",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Call Section
            Text(
                text = "Who would you like to call?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.targetId,
                onValueChange = { viewModel.targetId = it },
                label = { Text("Enter Target User ID") },
                placeholder = { Text("Recipient's unique ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.isStartingCall) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (viewModel.targetId.isNotBlank()) {
                                viewModel.startCall(
                                    viewModel.targetId,
                                    false
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = viewModel.targetId.isNotBlank()
                    ) {
                        Icon(Icons.Default.Videocam, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Video", style = MaterialTheme.typography.titleMedium)
                    }

                    Button(
                        onClick = {
                            if (viewModel.targetId.isNotBlank()) {
                                viewModel.startCall(
                                    viewModel.targetId,
                                    true
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = viewModel.targetId.isNotBlank()
                    ) {
                        Icon(Icons.Default.Call, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voice", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.5f))
        }
    }
}
