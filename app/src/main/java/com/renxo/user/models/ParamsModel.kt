package com.renxo.user.models

import com.renxo.user.utils.DynamicMapSerializer
import com.renxo.user.utils.DynamicToStringMapSerializer
import com.renxo.user.utils.getMap
import com.renxo.user.utils.json
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

@Serializable
data class ParamModel(
    val type: String,
    val action: String,
    val transaction: String? = null,
    @Serializable(with = DynamicMapSerializer::class)
    val params: HashMap<String, @Contextual Any?>? = null,
    @Serializable(with = DynamicMapSerializer::class)
    val payload: HashMap<String, @Contextual Any?>? = null,
)

@Serializable
data class Result(
    val code: String? = null,
    val variables: HashMap<String, String?>? = null,
)


@Serializable
data class ResponseModel(
    val type: String,
    val action: String,
    val transaction: String? = null,
    val orig_action: String? = null,
    val result: Result? = null,
    @Serializable(with = DynamicToStringMapSerializer::class)
    val params: HashMap<String, String?>? = null,

)




/////////////////////


