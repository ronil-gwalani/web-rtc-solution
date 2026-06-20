package com.renxo.user.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetAlertDialogue
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.viewmodels.WorkSelectionVM

@Composable
fun WorkSelectionScreen(
    viewModel: WorkSelectionVM,
    onBack: (Boolean) -> Unit,
    onNavigateToTask: () -> Unit,
) {
    BackHandler {
        onBack(viewModel.isWorkAccepted)
    }
    val homeVM = LocalHomeViewModelProvider.current
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is WorkSelectionVM.TaskAccepted -> {
                    homeVM.initialTaskInfoData(event.task)
                }
            }
        }
    }

    if (viewModel.showConfirmationDialogue.show) {
        WarningDialogueForNextHop(
            viewModel.showConfirmationDialogue.message,
            onButton1Click = {
                viewModel.showConfirmationDialogue =
                    viewModel.showConfirmationDialogue.copy(show = false)
                onBack(viewModel.isWorkAccepted)
            }, onButton2Click = {
                viewModel.acceptWarning()
            })
    }



    if (viewModel.showWarningDialogue) {
        WarningDialogForArea(onOk = {
            viewModel.showWarningDialogue = false
            onBack(true)
        })
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {


        if (viewModel.workList.isNotEmpty()) {
            if (viewModel.showProcessing) {
                CircularProgressIndicator()
            } else {
                val task = viewModel.workList.first()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.cardColor,
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = task.group_type ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textColor
                        )
                        val description = task.let { response ->
                            stringResource(
                                R.string.from_area_priority_next_hop_total_quantity,
                                response.from_area.toString(),
                                response.priority.toString(),
                                response.next_hop.toString(),
                                response.total_quantity.toString()
                            )
                        }
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Button(
                                onClick = { viewModel.onRejectTask() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(stringResource(R.string.reject))
                            }
                            Button(
                                onClick = {
                                    viewModel.acceptTask()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00B894)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(stringResource(R.string.accept))
                            }

                        }
                    }
                }

            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.no_task_available)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.onStartTask()
                onNavigateToTask()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.accentColor
            )
        ) {
            Text(stringResource(R.string.show_tasks))
        }
    }


}

@Composable
fun WarningDialogueForNextHop(
    message: String,
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit,
) {
    GetAlertDialogue {
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = stringResource(R.string.alert),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onButton1Click()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6E40)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center

                    )
                }
                Button(
                    onClick = {
                        onButton2Click()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B894)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.okay_text),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
private fun WarningDialogueForCompatibility(
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit,
) {
    GetAlertDialogue {
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = stringResource(R.string.alert),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.no_compatable_tasks),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onButton1Click()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6E40)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center

                    )
                }
                Button(
                    onClick = {
                        onButton2Click()
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B894)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.okay_text),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningDialogForArea(
    onOk: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = AppColors.backgroundColor,
        tonalElevation = 8.dp,
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = 8.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = stringResource(R.string.warning),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_task_for_this_area),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onOk,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B894)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.okay_text),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false
        )
    )
}