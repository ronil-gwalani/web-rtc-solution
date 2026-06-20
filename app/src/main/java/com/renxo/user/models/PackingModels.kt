package com.renxo.user.models


import com.renxo.user.utils.DefaultSerializer
import com.renxo.user.utils.ListSerializer
import kotlinx.serialization.Serializable


@Serializable
data class InventorLocation(
    val allocation_area: String? = null,
    val location: String? = null,
)

@Serializable
data class InitialLpnPayload(
    val id: String? = null,
    val last_stock_update: String? = null,
    val lpn: String? = null,
    val owner: String? = null,
    val inventory_location: InventorLocation? = null,
    val sub_inventory: List<SubInventory?>? = null,
    val location: String? = null,
)

@Serializable
data class BoxModel(
    val id: String? = null,
    val lpn: String? = null,
    val isSuggested: Boolean = false,
)


@Serializable
data class SubInventory(
    val inv_detail_id: String? = null,
    val detail: InventoryDetailItem? = null,
    val sub_lpn: String? = null,
)

@Serializable
data class InventoryDetailItem(
    val batch_name: String? = null,
    val exp_date: String? = null,
    val ibd: String? = null,
    val mfg_date: String? = null,
    val quantity: Int? = null,
    val order: String? = null,
    val product_id: String? = null,
    val serial_name: String? = null,
    val velocity: String? = null,
    val client: String? = null,
    val country_of_origin: String? = null,
    val family: String? = null,
)


@Serializable
data class InitializePackingWorkFlow(
    val attribute_name: String? = null,
    val data_type: String? = null,
    @Serializable(with = DefaultSerializer::class)
    val value: String? = null,
    @Serializable(with = ListSerializer::class)
    val list_of_values: List<String?>? = null,
    val mandatory: Boolean? = null,
    val prompted: Boolean? = null,
    val identification: Boolean? = null,
)



