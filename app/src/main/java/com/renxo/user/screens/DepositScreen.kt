package com.renxo.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.R
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalHomeViewModelProvider
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShackBarState
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.utils.showToast
import com.renxo.user.viewmodels.DepositVM
import androidx.compose.foundation.layout.FlowRow as FlowRow1


@Composable
fun DepositScreen(viewModel: DepositVM = viewModel(), finish: () -> Unit) {
    val snackBar = LocalSnackBar.current
    val context = LocalContext.current
    val homeVM = LocalHomeViewModelProvider.current
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is ShowSnackBar -> {
                    if (event.type == ShackBarState.ShackBarType.NEGATIVE) {
                        snackBar.showSnackBar(context.getString(event.message))

                    } else {
                        context.showToast(event.message)
                    }
                }

                is DepositVM.Finish -> {
                    finish()
                }
            }
        }
    }

    val selectedArea = viewModel.selectArea.collectAsState().value
    LaunchedEffect(homeVM.selectedArea) {
        viewModel.setSelectedArea(homeVM.selectedArea)
    }
    if (viewModel.showSubmitDialogue) {
        SubmitDialogue(viewModel, selectedArea)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppColors.backgroundColor)
            .padding(10.dp)
    ) {

        Button(
            onClick = {
                viewModel.deposit()
            }, modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.deposit))
        }

        TextField(
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors(),
            singleLine = true,

            placeholder = {
                Text(
                    text = stringResource(R.string.search),
                    fontSize = 16.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            value = viewModel.scannedItem,
            onValueChange = {
                viewModel.scannedItem = it
                viewModel.updateSelectAllState()
            },
            modifier = Modifier
                .padding(10.dp)
                .getTextFieldModifier()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.Tab -> {
                                viewModel.itemScanned()
                                true
                            }

                            else -> false
                        }
                    } else false
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
            ),
        )

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Checkbox(
                checked = viewModel.selectAll,
                onCheckedChange = {
                    viewModel.handleSelectAll(it)
                })

            Text(
                if (viewModel.selectAll) stringResource(R.string.unselect_all) else stringResource(R.string.select_all),
                modifier = Modifier.clickable {
                    viewModel.handleSelectAll(!viewModel.selectAll)
                })


            Checkbox(checked = selectedArea == null, onCheckedChange = {
                viewModel.handelAreaSelection(selectedArea, homeVM.selectedArea)
            })
            Text(
                stringResource(R.string.all_areas),
                modifier = Modifier
                    .padding(horizontal = 1.dp, vertical = 10.dp)
                    .clickable {
                        viewModel.handelAreaSelection(selectedArea, homeVM.selectedArea)
                    })
        }
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true,
            columns = GridCells.Fixed(2)
        ) {
            val filteredList = viewModel.list.filter { item ->
                (selectedArea?.let { it == item.work_area }
                    ?: true) && item.lpn?.contains(viewModel.scannedItem, ignoreCase = true) == true
            }
            itemsIndexed(filteredList, key = { index, item ->
                item.id + index
            }) { _, item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.whiteColor),
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))  // Add this line to clip the hit area
                        .clickable {
                            viewModel.updateItemCheckedState(item, !item.checked)
                        }
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { isChecked ->
                                // Update the list item through the ViewModel
                                viewModel.updateItemCheckedState(item, isChecked)
                            }
                        )

                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                            Text(
                                text = item.lpn ?: "",
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (selectedArea == null) {
                                    Text(
                                        text = item.work_area ?: "",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Normal,
                                            color = AppColors.accentColor,
                                            fontSize = 14.sp
                                        )
                                    )
                                } else {
                                    Text(
                                        text = item.location ?: "",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Normal,
                                            color = AppColors.accentColor,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitDialogue(vm: DepositVM, selectedArea: String?) {
    val homeVM = LocalHomeViewModelProvider.current
    val filteredList = vm.list.filter { item ->
        (selectedArea?.let { it == item.work_area && item.checked }
            ?: true) && item.lpn?.contains(vm.scannedItem, ignoreCase = true) == true
    }

    BasicAlertDialog(onDismissRequest = { }) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(20.dp))
                .background(AppColors.backgroundColor)
                .padding(vertical = 30.dp, horizontal = 20.dp),
        ) {
            Text(
                stringResource(R.string.deposit),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(17.dp))
            Text(
                text = stringResource(R.string.selected_area, homeVM.selectedArea),
                modifier = Modifier.padding(horizontal = 13.dp, 2.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            filteredList.takeIf { it.isNotEmpty() }.let {
                FlowRow1(modifier = Modifier.padding(8.dp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 5.dp, 5.dp),
                        text = stringResource(R.string.selected_lpn_s),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    it?.forEach { item ->
                        Text(
                            item.lpn.toString(),
                            modifier = Modifier
                                .padding(5.dp)
                                .clip(
                                    RoundedCornerShape(5.dp)
                                )
                                .background(AppColors.accentColor.copy(alpha = 0.3f))
                                .padding(horizontal = 4.dp)
                        )
                    }

                }


            }
            Spacer(Modifier.height(5.dp))

            TextField(
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                singleLine = true,

                placeholder = {
                    Text(
                        text = vm.suggestedLocation.takeIf { it.isNotEmpty() }
                            ?: stringResource(R.string.scan_here),
                        fontSize = 16.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                value = vm.submitScan,
                onValueChange = {
                    vm.submitScan = it
                },
                modifier = Modifier
                    .padding(10.dp)
                    .getTextFieldModifier()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Tab -> {
                                    vm.confirmSubmit(
                                        homeVM.selectedArea,
                                        filteredList.map { it.id })
                                    true
                                }

                                else -> false
                            }
                        } else false
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {

                Button(
                    onClick = {
                        vm.confirmSubmit(homeVM.selectedArea, filteredList.map { it.id })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.green),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp)
                ) { Text(stringResource(R.string.submit), color = AppColors.accentColor) }
                Button(
                    onClick = { vm.showSubmitDialogue = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.red),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = AppColors.whiteColor)
                }

            }
        }

    }

}
