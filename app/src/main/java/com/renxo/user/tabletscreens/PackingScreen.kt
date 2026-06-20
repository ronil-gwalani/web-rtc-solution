package com.renxo.user.tabletscreens

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renxo.user.R
import com.renxo.user.dynamicUI.DynamicFormItem
import com.renxo.user.dynamicUI.InputData
import com.renxo.user.dynamicUI.InputType
import com.renxo.user.dynamicUI.Requirements
import com.renxo.user.models.InitializePackingWorkFlow
import com.renxo.user.models.SubInventory
import com.renxo.user.ui.theme.AppColors
import com.renxo.user.utils.GetOneTimeBlock
import com.renxo.user.utils.LocalSnackBar
import com.renxo.user.utils.ShowSnackBar
import com.renxo.user.utils.getTextFieldModifier
import com.renxo.user.utils.getTextFiledColors
import com.renxo.user.viewmodels.PackingVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PackingScreen(
    viewModel: PackingVM
) {
    val context = LocalContext.current
    val snackBarState = LocalSnackBar.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val animatedLeftSide by animateFloatAsState(
        targetValue = viewModel.leftSide,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    fun getDataForUI(
        context: Context, questions: List<InitializePackingWorkFlow?>
    ): ArrayList<InputData> {
        val list = ArrayList<InputData>()
        questions.forEach { question ->
            val value =
                if (!question?.value.isNullOrEmpty() && question?.value != "null") {
                    question?.value
                } else {
                    ""
                }
            val toShow =
                !(question?.identification == false && question.mandatory == true && question.prompted == false && value?.isNotEmpty() == true)

            val editable =
                if (question?.identification == true && !question.value.isNullOrEmpty()) false else
                    (question?.prompted == true) || (question?.prompted == false && question.mandatory == true && question.value.isNullOrEmpty())
            when (question?.data_type?.lowercase() ?: "unknown") {

                DataTypes.money, DataTypes.weekday, DataTypes.month, DataTypes.statuses, DataTypes.family, DataTypes.velocity -> {
                    val default = question?.list_of_values
                    list.add(
                        InputData(
                            type = InputType.DROPDOWN,
                            requirements = Requirements.DropDownRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                                options = default,
                                defaultValue = question?.value.toString()
                            ),
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: true,
                            editable = editable, value = value
                        )
                    )
                }

                DataTypes.id, DataTypes.string, DataTypes.text -> {

                    list.add(
                        InputData(
                            type = InputType.EDIT_TEXT,
                            requirements = Requirements.EditTextRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: true,
                            editable = editable, value = value
                        )
                    )
                }


                DataTypes.number -> {
                    list.add(
                        InputData(
                            type = InputType.EDIT_TEXT_NUMBER,
                            requirements = Requirements.EditTextNumberRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: true,
                            editable = editable, value = value
                        )
                    )
                }

                DataTypes.date -> {
                    list.add(
                        InputData(
                            type = InputType.DATE,
                            requirements = Requirements.DateRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            hint = question?.value,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: false,
                            editable = editable, value = value
                        )
                    )
                }

                DataTypes.datetime -> {
                    list.add(
                        InputData(
                            type = InputType.DATE_TIME,
                            requirements = Requirements.DateTimeRequirements(
                                error = context.getString(
                                    R.string.please_provide,
                                    question?.attribute_name
                                ),
                            ),
                            hint = question?.value,
                            toShow = toShow,
                            placeholder = question?.attribute_name ?: "",
                            required = question?.mandatory ?: false,
                            editable = editable, value = value
                        )
                    )
                }

                else -> {
                    Log.e("getData", "Unknown or unsupported data type: ${question?.data_type}")
                }
            }
        }
        return list
    }

    fun sortPackingWorkFlow(items: List<InitializePackingWorkFlow?>): List<InitializePackingWorkFlow?> {
        val priorityOrder = listOf("product_id", "quantity") // Define priority order
        return items.sortedBy {
            priorityOrder.indexOf(it?.attribute_name).takeIf { index -> index >= 0 }
                ?: Int.MAX_VALUE
        }
    }
    GetOneTimeBlock {
        viewModel.uiEventsFlow.collect { event ->
            when (event) {
                is PackingVM.HideKeyBoard -> {
                    keyboardController?.hide()
                }

                is PackingVM.GetDataForUI -> {

                    viewModel.uiElements.addAll(
                        getDataForUI(
                            context, sortPackingWorkFlow(event.list)
                        )
                    )
                }

                is PackingVM.CustomPackingSnackBar -> {
                    snackBarState.showSnackBar(
                        context.getString(
                            event.message,
                            event.placeHolder
                        )
                    )
                }

                is ShowSnackBar -> {
                    snackBarState.showSnackBar(context.getString(event.message))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .background(AppColors.backgroundColor)
            .clickable(
// Only trigger when floating and clicking outside the drawer
                indication = null, // This removes the ripple effect
                interactionSource = remember { MutableInteractionSource() },
                enabled = viewModel.showOverLay,
                onClick = {
                    viewModel.showOverLay = false
                })
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.whiteColor),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.packing_screen), style = TextStyle(
                    fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black
                ), modifier = Modifier
//                    .weight(1f)
                    .padding(horizontal = 20.dp)
            )

            Text(
                stringResource(R.string.inventory_details) + viewModel.inventoryLpn,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
//                    .weight(1f)
//                   .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.LightGray)
                    .padding(8.dp),
                style = TextStyle(
                    fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            )
            TextField(
                shape = RoundedCornerShape(15.dp),
                colors = getTextFiledColors(),
                value = viewModel.toSearchQuery,
                onValueChange = {
                    viewModel.toSearchQuery = it
                },
                label = { Text(stringResource(R.string.scan_box_here)) },
                singleLine = true,
                modifier = Modifier
                    .padding(bottom = 5.dp, end = 5.dp)
                    .focusRequester(viewModel.boxFocusRequester)
                    .border(
                        width = if (viewModel.focus) 2.dp else 1.dp,
                        color = if (viewModel.focus) AppColors.accentColor else AppColors.hintColor,
                        shape = RoundedCornerShape(15.dp),
                    )
                    .onFocusChanged { focusState ->
                        viewModel.focus = focusState.isFocused
                    }
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            if (event.key == Key.Tab) {
                                viewModel.selectedBox =
                                    viewModel.boxes.firstOrNull { it.lpn == viewModel.toSearchQuery }
                                viewModel.packInventory()
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.selectedBox =
                            viewModel.boxes.firstOrNull { it.lpn == viewModel.toSearchQuery }

                        viewModel.packInventory()

                    }) {
                        Icon(
                            modifier = Modifier.padding(5.dp),
                            painter = painterResource(R.drawable.ic_scan),
                            contentDescription = (stringResource(id = R.string.search)),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                })
            GetOneTimeBlock {
                keyboardController?.hide()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.89f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(animatedLeftSide)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
//                                .weight(animatedLeftSide)
                                .padding(end = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.cardColor
                            )
                        ) {
                            LeftSection(
                                Modifier.fillMaxSize(),
                                viewModel
                            ) { productId, quantity, subInventoryId, subLpn ->
                                viewModel.initializePacking(
                                    productId,
                                    quantity,
                                    subInventoryId = subInventoryId,
                                    subLpn = subLpn
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp) // Icon size
                                .align(Alignment.TopEnd) // Align to the top-end of the parent Box
                                .clip(RoundedCornerShape(50)) // Circular shape
                                .background(AppColors.cardColor)
//                                .border(2.dp, AppColors.accentColor, RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.manageLeftSide()
                                }, contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                tint = AppColors.accentColor,
                                imageVector = if (!viewModel.leftSideExpended) {
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight

                                } else {
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(viewModel.middleSide)
                            .padding(start = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.cardColor
                        )
                    ) {
                        MiddleSection(
                            Modifier.fillMaxSize(),
                            viewModel,
                            initializePacking = { viewModel.initializePacking(it, "1", "", "") },
                            updateInventory = {
                                viewModel.updateInventory()
                            },
                            unpackInventory = {
                                viewModel.unPackInventory()
                            })
                    }
                    if (viewModel.isPinned) {
                        Card(
                            modifier = Modifier
                                .weight(viewModel.rightSide)
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.cardColor
                            )
                        ) {
                            RightSection(
                                Modifier.fillMaxSize(),
                                viewModel
                            ) { _, quantity, subInventoryId, subLpn ->
                                viewModel.modifyInventory(quantity, subInventoryId)
                            }
                        }
                    }
                }
                if (viewModel.showOverLay) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .align(Alignment.CenterEnd)
                            .padding(start = 8.dp)
                            .clickable {} // Prevent clicks from propagating to the parent
                    ) {
                        Card(
                            modifier = Modifier.fillMaxHeight(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3E6E6)
                            )
                        ) {
                            RightSection(
                                Modifier.fillMaxSize(),
                                viewModel
                            ) { _, quantity, subInventoryId, subLpn ->
                                viewModel.modifyInventory(quantity, subInventoryId)
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.11f)
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.cardColor
                )
            ) {
                BoxSection(Modifier.fillMaxSize(), viewModel, createCarton = {
                    viewModel.createCarton()
                }, packInventory = {
                    viewModel.packInventory()
                }, fetchScannedInventory = {
                    viewModel.uiElements.clear()
                    viewModel.fetchScannedInventory()
                })
            }
        }
    }
}

@Composable
fun LeftSection(
    modifier: Modifier,
    viewModel: PackingVM,
    initializePacking: (String, String, String, String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        if (viewModel.leftSideExpended) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                listOf(
                    "Product id",
                    "Quantity",
                    "Order",
                    "IBD",
                    "Batch Name"
                ).forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {

            itemsIndexed(viewModel.inventoryList, key = { index, item ->
                item?.inv_detail_id + index
            }) { _, inventory ->
                if (viewModel.leftSideExpended) {
                    ExpandedView(viewModel, inventory, initializePacking)
                } else {
                    CompactView(viewModel, inventory, initializePacking)
                }
            }
        }
    }
}

@Composable
fun MiddleSection(
    modifier: Modifier,
    viewModel: PackingVM,
    initializePacking: (String) -> Unit,
    updateInventory: () -> Unit,
    unpackInventory: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var textFieldFocused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
//            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) {
                if (viewModel.showOverLay) {
                    viewModel.showOverLay = false
                }
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // textfield for scan for work
        TextField(
            shape = RoundedCornerShape(15.dp),
            colors = getTextFiledColors(),
            singleLine = true,
            label = { Text(stringResource(id = R.string.scan_here)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .getTextFieldModifier()
                .focusRequester(viewModel.skuFocusRequester)
                .onFocusChanged { focusState ->
                    textFieldFocused = focusState.isFocused
                    if (focusState.isFocused && viewModel.showOverLay) {
                        viewModel.showOverLay = false
                    }
                }
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        if (event.key == Key.Tab) {
                            initializePacking(viewModel.scannedField)
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                },

            value = viewModel.scannedField,
            onValueChange = {
                viewModel.scannedField = it
                if (viewModel.showOverLay) {
                    viewModel.showOverLay = false
                }
            },

            trailingIcon = {
                IconButton(onClick = {

                    initializePacking(viewModel.scannedField)
                }) {
                    Icon(
                        modifier = Modifier.padding(5.dp),
                        painter = painterResource(R.drawable.ic_scan),
                        contentDescription = (stringResource(id = R.string.search)),
                        tint = MaterialTheme.colorScheme.primary

                    )
                }
            }
        )

        GetOneTimeBlock {
            viewModel.skuFocusRequester.requestFocus()
            keyboardController?.hide()
        }
        Spacer(modifier = Modifier.height(16.dp))


        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (viewModel.showOverLay) {
                                viewModel.showOverLay = false
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                            }
                    ) {
                        TextField(
                            value = viewModel.productId,
                            shape = RoundedCornerShape(15.dp),
                            colors = getTextFiledColors(),
                            onValueChange = { newValue ->

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            enabled = false,
                            label = { Text("Product ID") },
                            modifier = Modifier
                                .getTextFieldModifier()

                        )
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (viewModel.showOverLay) {
                                viewModel.showOverLay = false
                            }
                        }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
//                                onFocusChanged(true)
                            }
                    ) {
                        TextField(
                            value = viewModel.staticQuantity,
                            shape = RoundedCornerShape(15.dp),
                            colors = getTextFiledColors(),
                            onValueChange = { newValue ->
                                val parsedValue = newValue.toIntOrNull()
                                if (parsedValue != null) {
                                    if (parsedValue in 0..1000000000) {
                                        viewModel.staticQuantity = parsedValue.toString()
                                    }
                                } else if (newValue.isEmpty() || newValue == "null") {
                                    viewModel.staticQuantity = ""
                                }

//                                onFocusChanged(true)

                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            enabled = viewModel.quantityEditable,
                            label = { Text("Quantity") },
                            modifier = Modifier
                                .getTextFieldModifier()

                        )
                    }

                }
            }

            itemsIndexed(
                viewModel.uiElements,
//                key = { index, item -> item.placeholder + index }
            ) { index, item ->
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            if (viewModel.showOverLay) {
//                                viewModel.showOverLay = false
//                            }
//                        }
//                ) {
                DynamicFormItem(
                    inputData = item,
                    onFocusChanged = { focused ->
                        if (focused && viewModel.showOverLay) {
                            viewModel.showOverLay = false
                        } else if (!focused && viewModel.partialSubmitAllowed && item == viewModel.uiElements.last()) {
                            viewModel.doPartialSubmit()
                        }
                    },
                    onValueChange = { value ->
                        viewModel.updateInputValue(index, value)
                        if (viewModel.showOverLay) {
                            viewModel.showOverLay = false
                        }

                    }
                )
//                }
            }
        }


        if (viewModel.uiElements.isNotEmpty() && viewModel.showUnpackButtons) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = {
                    updateInventory()
                }) {
                    Text("Update")
                }
                Button(onClick = {
                    unpackInventory()
                }) {
                    Text("Unpack")
                }
            }
        }
    }
}

@Composable
fun RightSection(
    modifier: Modifier,
    viewModel: PackingVM,
    initializePacking: (String, String, String, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Add TextField at the top
            Text(
                "Scanned Inventory",
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.LightGray)
                    .padding(8.dp),
                style = TextStyle(
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 5.dp)
            ) {
                itemsIndexed(viewModel.scannedInventoryList, key = { index, item ->
                    item?.inv_detail_id + index
                }) { _, inventory ->
                    CompactView(viewModel, inventory, initializePacking)
                }
            }
        }

        Icon(
            painter = painterResource(
                if (viewModel.showOverLay) R.drawable.pin
                else R.drawable.unpin
            ), contentDescription = null, modifier = Modifier
                .size(30.dp)
                .clickable {
                    viewModel.manageRightSide()
                }
                .border(2.dp, AppColors.accentColor, RoundedCornerShape(50))
                .padding(8.dp))
    }
}

@Composable
fun BoxSection(
    modifier: Modifier,
    viewModel: PackingVM,
    createCarton: () -> Unit,
    packInventory: () -> Unit,
    fetchScannedInventory: () -> Unit
) {
    val snackBarState = LocalSnackBar.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sortedBoxes = viewModel.boxes.sortedBy { box ->
                    if (viewModel.suggestedBoxes.contains(box)) 0 else 1
                }
                itemsIndexed(sortedBoxes, key = { index, item ->
                    item.id + index
                }) { index, box ->
                    val isSelected = viewModel.selectedBox == box
                    val isSuggested = viewModel.suggestedBoxes.contains(box)
                    val isNewlyAdded = box.lpn == viewModel.lastAddedBox

                    var offsetY by remember { mutableFloatStateOf(if (isNewlyAdded) -200f else 0f) }
                    var scale by remember { mutableFloatStateOf(if (isNewlyAdded) 0.3f else 1f) }

                    LaunchedEffect(isNewlyAdded) {
                        if (isNewlyAdded) {
                            launch {
                                animate(
                                    initialValue = -200f,
                                    targetValue = 0f,
                                    animationSpec = tween(500)
                                ) { value, _ -> offsetY = value }
                            }
                            launch {
                                animate(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = tween(500)
                                ) { value, _ -> scale = value }
                            }
                            delay(500)
                            viewModel.lastAddedBox = null
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(scale)
                            .offset { IntOffset(x = 0, y = -offsetY.roundToInt()) }
                            .size(120.dp)
                            .offset(x = if (isSelected) (-13).dp else 0.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = when {
                                    isSelected -> AppColors.accentColor.copy(alpha = 0.2f)
                                    isSuggested -> AppColors.hintColor.copy(alpha = 0.1f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }, shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = if (isSuggested) 2.dp else 1.dp, color = when {
                                    isSelected -> AppColors.accentColor
                                    isSuggested -> AppColors.hintColor
                                    else -> Color.Gray.copy(alpha = 0.4f)
                                }, shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                if (isSuggested) {
                                    viewModel.selectedBox = box
                                    packInventory()
                                } else {
                                    if (viewModel.uiElements.isNotEmpty()) {
                                        return@clickable
                                    }

                                    if (!viewModel.isPinned) {
                                        focusManager.clearFocus()
                                        viewModel.showOverLay = true
                                    }
                                    viewModel.selectedBox = box
                                    fetchScannedInventory()
                                }
                            }) {
                        Text(
                            text = box.lpn.toString(),
                            color = when {
                                isSelected -> AppColors.accentColor
                                isSuggested -> AppColors.hintColor
                                else -> Color.DarkGray.copy(alpha = 0.8f)
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            IconButton(
                onClick = { viewModel.showAddCartonDialog = true },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.accentColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_box),
                    tint = AppColors.whiteColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }

    if (viewModel.showAddCartonDialog) {
        BoxDialog(newLpn = viewModel.newLpn, onLpnChange = { viewModel.newLpn = it }, onConfirm = {
            if (viewModel.newLpn.isNotEmpty()) {
                createCarton()
            } else {
                snackBarState.showSnackBar(context.getString(R.string.please_fill_lpn))
            }
            viewModel.adjustFocus()
        }, onDismiss = {
            viewModel.showAddCartonDialog = false
            viewModel.newLpn = ""
            viewModel.adjustFocus()
        })
    }
}

@Composable
fun BoxDialog(
    newLpn: String,
    onLpnChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val packingFocusRequester = remember { FocusRequester() }
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = stringResource(R.string.generate_lpn), fontWeight = FontWeight.Bold
        )
    }, text = {
        TextField(
            shape = RoundedCornerShape(15.dp),
            value = newLpn,
            onValueChange = onLpnChange,
            label = { Text(stringResource(R.string.enter_lpn)) },
            placeholder = {
                Text(
                    text = stringResource(R.string.your_answer_here),
                    fontSize = 16.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            colors = getTextFiledColors(),
            modifier = Modifier
                .getTextFieldModifier()
                .focusRequester(packingFocusRequester)
        )
        GetOneTimeBlock {
            packingFocusRequester.requestFocus()
        }
    }, confirmButton = {
        Button(onClick = onConfirm) {
            Text(stringResource(R.string.confirm))
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    })
}

@Composable
fun CompactView(
    viewModel: PackingVM,
    subInventory: SubInventory?,
    initializePacking: (String, String, String, String) -> Unit
) {
    var showMore by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = {
//                        viewModel.lpnNum= inventory?.sku.toString()
            viewModel.productId = subInventory?.detail?.product_id.toString()
            viewModel.inventoryId = subInventory?.inv_detail_id.toString()
            initializePacking(
                viewModel.productId,
                subInventory?.detail?.quantity.toString(),
                viewModel.inventoryId,
                subInventory?.sub_lpn.toString()
            )
        }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (!showMore) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // product_id value
                    Text(
                        text = subInventory?.detail?.product_id ?: "-", style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ), modifier = Modifier.weight(1f)
                    )
                    // Quantity value
                    Text(
                        text = subInventory?.detail?.quantity?.toString() ?: "-", style = TextStyle(
                            fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.DarkGray
                        ), modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Default fields (product_id and Quantity)
                KeyValueRow("Product ID", subInventory?.detail?.product_id ?: "-")
                KeyValueRow("Quantity", subInventory?.detail?.quantity?.toString() ?: "-")
                // Additional fields
                subInventory?.detail?.batch_name?.let {
                    KeyValueRow("Batch", it)
                }
                subInventory?.detail?.client?.let {
                    KeyValueRow("Client", it)
                }
                subInventory?.detail?.country_of_origin?.let {
                    KeyValueRow("Country_of_Origin", it)
                }
                subInventory?.detail?.exp_date?.let {
                    KeyValueRow("Exp_date", it)
                }
                subInventory?.detail?.family?.let {
                    KeyValueRow("Family", it)
                }
                subInventory?.detail?.ibd?.let {
                    KeyValueRow("IBD", it)
                }
                subInventory?.detail?.mfg_date?.let {
                    KeyValueRow("Mfg_date", it)
                }
                subInventory?.detail?.order?.let {
                    KeyValueRow("Order", it)
                }
                subInventory?.detail?.serial_name?.let {
                    KeyValueRow("Serial_name", it)
                }
                subInventory?.detail?.velocity?.let {
                    KeyValueRow("Velocity", it)
                }
            }

            Text(
                text = if (showMore) stringResource(R.string.show_less) else stringResource(R.string.show_more),
                color = Color.Blue,
                modifier = Modifier
                    .clickable { showMore = !showMore }
                    .padding(top = 4.dp)
                    .align(Alignment.End),
                style = TextStyle(fontSize = 14.sp))
        }
    }
}

@Composable
private fun ExpandedView(
    viewModel: PackingVM,
    subInventory: SubInventory?,
    initializePacking: (String, String, String, String) -> Unit
) {
    val cellTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 12.sp
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                viewModel.productId = subInventory?.detail?.product_id.toString()
                viewModel.inventoryId = subInventory?.inv_detail_id.toString()
                initializePacking(
                    viewModel.productId,
                    subInventory?.detail?.quantity.toString(),
                    viewModel.inventoryId,
                    subInventory?.sub_lpn.toString()
                )
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // product_id Column
            Text(
                text = subInventory?.detail?.product_id ?: "-",
                style = cellTextStyle.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            // Quantity Column
            Text(
                text = subInventory?.detail?.quantity?.toString() ?: "-",
                style = cellTextStyle,
                modifier = Modifier.weight(0.7f),
                textAlign = TextAlign.Start
            )
            // order Column
            Text(
                text = subInventory?.detail?.order ?: "-",
                style = cellTextStyle,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Start
            )
            // ibd Column
            Text(
                text = subInventory?.detail?.ibd ?: "-",
                style = cellTextStyle,
//                maxLines = 1,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Justify
            )
            // Batch Column
            Text(
                text = subInventory?.detail?.batch_name ?: "-",
                style = cellTextStyle,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun KeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key, style = TextStyle(
                fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Gray
            ), modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value, style = TextStyle(
                fontSize = 13.sp, fontWeight = FontWeight.Normal
            ), modifier = Modifier.weight(0.6f)
        )
    }
}

private object DataTypes {
    const val id = "id"               // String AlphaNumeric
    const val text = "text"           //String
    const val string = "string"           //String
    const val number = "number"       // Number
    const val money = "money"         // DropDown
    const val velocity = "velocity"         // DropDown
    const val address = "address"     // TODO
    const val date = "date"           // Calendars
    const val datetime = "datetime"    // Calendar And Timing
    const val time = "time"            // Time Picker
    const val weekday = "weekday"      // DropDown
    const val month = "month"          // DropDown
    const val family = "family"          // DropDown
    const val toggle = "toggle"        // Radio
    const val range = "range"          // TODO
    const val statuses = "statuses"          // DropDown
}

