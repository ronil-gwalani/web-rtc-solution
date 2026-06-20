package com.renxo.user.models

import kotlinx.serialization.Serializable

@Serializable
data class UnloadDeliveryPayloadResult(
//    val areas: List<Areas?>? = null,
    val location: String? = null,
//    val type: String? = null
)

//@Serializable
//data class Areas(
//    val area: String? = null
//)
@Serializable
data class UnloadDeliveryOutput(
    val appointments_update: AppointmentsUpdate? = null,
    val inbound_deliveries_update: InboundDeliveriesUpdate? = null,
    val trailers_update: TrailersUpdate? = null
)

@Serializable
data class AppointmentsUpdate(
    val id: String? = null,
    val inbound_delivery: String? = null,
    val status: String? = null
)

@Serializable
data class InboundDeliveriesUpdate(
    val id: String? = null,
    val location: String? = null,
    val status: String? = null,
    val transport_equipment: String? = null
)

@Serializable
data class TrailersUpdate(
    val location: String? = null,
    val lpn: String? = null,
    val status: String? = null
)

@Serializable
data class UnloadDeliveryResult(
    val ASN_type: String? = null,
    val ibd: String? = null,
    val is_workflow_completed: Boolean? = null,
    val location: String? = null,
    val orders: List<Order>? = null,
    val status: String? = null,
    val supplier: String? = null,
    val transport_equipment: String? = null,
    val type: String? = null,
)

@Serializable
data class Order(
    val order: String? = null,
    val lines: List<OrderLine>? = null
)

@Serializable
data class OrderLine(
    val expected: Int? = null,
    val line_id: String? = null,
    val product: String? = null,
    val received: Int? = null,
    val sku: String? = null
)
