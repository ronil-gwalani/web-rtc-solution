package com.renxo.user.testing


import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renxo.user.dynamicUI.DynamicFormItem


@Composable
fun Testing(viewModel: TestingVM = viewModel()) {
    DynamicList(viewModel)
}



@Composable
fun DynamicList(vm: TestingVM) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Button(onClick = {
            Log.e("onClick", ": ${vm.getRequiredWorkflow()}")
        }) {
            Text("Hello Boss")
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
        ) {
            itemsIndexed(vm.uiElements, key = { index, item ->
                item.placeholder + index
            }) { index, item ->

                DynamicFormItem(
                    inputData = item,
                    onFocusChanged = { focused ->

                    },
                    onValueChange = { value ->
                        vm.updateInputValue(index, value)
                    }
                )
            }

        }
    }
}


@Composable
private fun FlowRowSimpleUsageExample() {
    FlowRow(modifier = Modifier.padding(8.dp)) {
        Text("Price: High to Low")
        Text("Avg rating: 4+")
        Text("Free breakfast")
        Text("Free cancellation")
        Text("£50 pn")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBasicSample(
    items: List<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberPullToRefreshState()

    Scaffold() {
        PullToRefreshBox(
            modifier = modifier.padding(it),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = true,
                    state = state
                )
            }
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(items) {
                    ListItem({ Text(text = it) })
                }
            }
        }
    }
}