package com.renxo.user.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.models.WorkSelectionModel
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.viewmodels.AcceptedWorkVM

@Composable
fun AcceptedWorkScreen(
    viewModel: AcceptedWorkVM = viewModel(), onFinish: (Boolean) -> Unit
) {
    BackHandler {
        onFinish(viewModel.needToRefreshPreviousScreen)
    }
    val homeVM = LocalHomeViewModelProvider.current
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is AcceptedWorkVM.RemoveNextHop -> {
                    homeVM.initialTaskInfoData(WorkSelectionModel())
                }
            }
        }
    }
    Box(Modifier.fillMaxSize()) {

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            itemsIndexed(viewModel.acknowledgedWorkList, key = { index, item ->
                item.id + index
            }) { _, task ->
//                Text(task.toString())
                TaskCard(task) {
                    viewModel.cancelWork(it)
                }

            }
        }
    }

    if (viewModel.showWarningDialogue) {
        WarningDialog(onOk = {
            viewModel.showWarningDialogue = false
            onFinish(viewModel.needToRefreshPreviousScreen)
        })
    }
}

@Composable
private fun WarningDialog(
    onOk: () -> Unit
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
                    text = stringResource(R.string.no_accepted_tasks_found),
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


@Composable
private fun TaskCard(task: WorkSelectionModel, cancel: (String) -> Unit) {
//        var clicked by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Group Type Title
                Text(
                    text = task.group_type ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.textColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Task Details
                TaskDetailItem(label = stringResource(R.string.from_area), value = task.from_area)
                TaskDetailItem(
                    label = stringResource(R.string.priority_with_collen),
                    value = task.priority.toString()
                )
                TaskDetailItem(label = stringResource(R.string.next_hop), value = task.next_hop)
                TaskDetailItem(label = stringResource(R.string.status), value = task.status)

                Spacer(modifier = Modifier.height(12.dp))


            }

            // Divider for separation
            if (task.status == "Acknowledged") {
                HorizontalDivider(
                    thickness = 1.dp, color = AppColors.textColor.copy(alpha = 0.1f)
                )

//                Spacer(modifier = Modifier.height(8.dp))

                // Delete Button at the bottom
                task.id?.let { taskId ->
                    Text(
                        text = stringResource(R.string.cancel_work),
                        color = AppColors.whiteColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.red.copy(alpha = 0.8f))
                            .clickable {
                                if (!task.cancelled) {
                                    task.cancelled = true
                                    cancel(taskId)
                                }
                            }
                            .padding(10.dp),
                        textAlign = TextAlign.Center)

                }
            }

        }
    }

}

@Composable
private fun TaskDetailItem(label: String, value: String?) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textColor,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textColor.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}


