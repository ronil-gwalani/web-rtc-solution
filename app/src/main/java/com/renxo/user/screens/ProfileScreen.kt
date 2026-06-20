package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renxo.user.models.MenuOptionModel
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.WebSocketInterceptor

@Composable
fun ProfileMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    menuItemList: List<MenuOptionModel>,
    onMenuItemClicked: (MenuOptionModel) -> Unit,
    onLogoutClick: () -> Unit,

    ) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(AppColors.whiteColor), shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            menuItemList.forEach { tool ->
                DropdownMenuItem(text = {
                    Column {
                        Text(text = stringResource(tool.title))
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                top = 1.dp, bottom = 0.dp
                            )
                        )
                    }
                }, onClick = {
                    onMenuItemClicked(tool)
                })
            }
            ProfileMenuItem(
                icon = Icons.Default.Info,
                text = "Show Logs",
                onClick = { WebSocketInterceptor.showWebSocketResponses.value = true },
                tint = Color.Black
            )

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                text = "Logout",
                onClick = onLogoutClick,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = tint
            )
        }
    }
}