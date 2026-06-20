package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.R
import com.renxo.user.models.WorkType
import com.renxo.user.ui.theme.AppColors

@Composable
fun DirectedWorkScreen(
    taskList: List<WorkType>,
    onTaskClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.available_tasks),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 20.sp,
            ), fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(Modifier.fillMaxSize()) {

            itemsIndexed(taskList, key = { index, item ->
                item.type + index

            }) { _, task ->

                TaskCard(
                    type = task.type ?: "",
                    count = task.count ?: 0,
                    priority = task.priority ?: 0,
                    onClick = {
                        onTaskClick(task.type ?: "")
                    }
                )
            }
        }
    }

}

@Composable
private fun TaskCard(
    type: String,
    count: Int,
    priority: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 15.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.whiteColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Type with Icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.textColor
                )
            }

            // Count and Priority
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(R.string.count),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.textColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(R.string.priority),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.textColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = priority.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textColor
                    )
                }
            }
        }
    }
}