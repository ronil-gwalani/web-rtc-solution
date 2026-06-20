package com.renxo.user.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//TODO this is just for the backend to check logs of websocket this needs to be remove once the project is stable
object WebSocketInterceptor {
    val showWebSocketResponses = MutableStateFlow(false)

    private val jsonResponse = mutableStateListOf<Pair<String, String>>()
    fun clear() {
        jsonResponse.clear()
    }

    fun addJsonResponse(key: String, value: String) {
        if (jsonResponse.size >= 30) {
            jsonResponse.removeAt(0) // Remove the oldest item
        }
        jsonResponse.add(key to value) // Add the new item
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss:SSS", Locale.getDefault())
        return dateFormat.format(Date())
    }

    @Composable
    fun WebSocketJsonScreen(close: () -> Unit) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,

                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            // Add animation to the dialog
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Text(
                    modifier = Modifier.padding(5.dp),
                    text = "Responses from WebSocket",
                    fontSize = 20.sp,
                    color = Color.White
                )

                // 🔥 Ensure LazyColumn is inside a Box with weight(1f)
                LazyColumn(
                    reverseLayout = true,
                    modifier = Modifier.height(screenHeight - 100.dp)
                ) {
                    itemsIndexed(jsonResponse.reversed(), key = { index, item ->
                        item.first + index
                    }) { _, item ->
                        ResponseCard(item)
                    }

                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        close()
                    }) {
                        Text("Close")
                    }
                    Button(onClick = {
                        clear()
                    }) {
                        Text("Clear")
                    }
                }
            }


        }
    }

    @Composable
    private fun ResponseCard(item: Pair<String, String>) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.first,
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = formatJson(item.second),
                color = Color.Green,
                fontSize = 16.sp
            )
            HorizontalDivider(thickness = 1.dp, color = Color.White)
        }

    }

    @Composable
    fun WebSocketLogs(hideProfileMenu: () -> Unit) {
        val showWebSocketResponses = WebSocketInterceptor.showWebSocketResponses.collectAsState()
        showWebSocketResponses.value.let {
            if (it) {
                hideProfileMenu()
                WebSocketJsonScreen {
                    WebSocketInterceptor.showWebSocketResponses.value = false
                }
            }
        }
    }

    // Function to format JSON
// Function to format JSON using kotlinx.serialization
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonForPrint = Json { prettyPrint = true; prettyPrintIndent = "  " }

    private fun formatJson(jsonString: String): String {
        return try {
            val jsonElement = Json.parseToJsonElement(jsonString) // Parse JSON
            jsonForPrint.encodeToString(
                JsonObject.serializer(),
                jsonElement.jsonObject
            ) // Pretty-print JSON
        } catch (e: Exception) {
            "Invalid JSON $jsonString"
        }
    }
}

