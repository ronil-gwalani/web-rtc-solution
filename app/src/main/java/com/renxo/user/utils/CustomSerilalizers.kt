package com.renxo.user.utils

import com.renxo.user.models.CustomField
import com.renxo.user.models.CustomScreenAttributes
import com.renxo.user.models.InitializePackingWorkFlow
import com.renxo.user.models.PickingAttribute
import com.renxo.user.models.TaskInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
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



 object DynamicToStringMapSerializer : KSerializer<HashMap<String, String?>> {
    private val mapSerializer = MapSerializer(String.serializer(), JsonElement.serializer())

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: HashMap<String, String?>) {
        val map = value.mapValues { (_, value) ->
            JsonPrimitive(value)
        }
        encoder.encodeSerializableValue(mapSerializer, map)
    }

    override fun deserialize(decoder: Decoder): HashMap<String, String?> {
        val map = decoder.decodeSerializableValue(mapSerializer)
        return HashMap(map.mapValues { (_, element) ->
            when (element) {
                is JsonNull -> null
                is JsonPrimitive -> when {
                    element.isString -> element.content
                    else -> element.toString()
                }

                is JsonArray -> element.toString()
                is JsonObject -> element.toString()
            }
        })
    }
}


 object DynamicMapSerializer : KSerializer<HashMap<String, Any?>> {
    override val descriptor = buildClassSerialDescriptor("DynamicMap") {
        element(
            "map",
            buildClassSerialDescriptor(
                String.serializer().descriptor.toString(),
                JsonElement.serializer().descriptor
            )
        )
    }

    override fun serialize(encoder: Encoder, value: HashMap<String, Any?>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("This serializer can only be used with JSON")
        val jsonObject = buildJsonObject {

            value.forEach { (key, value) ->
                put(key, getTypeAccordingly(value))
            }
        }
        encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
    }

    override fun deserialize(decoder: Decoder): HashMap<String, Any?> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This serializer can only be used with JSON")
        val jsonObject = jsonDecoder.decodeJsonElement() as JsonObject
        return getMap {
            jsonObject.forEach { (key, element) ->
                put(
                    key, when (element) {
                        is JsonNull -> null
                        is JsonPrimitive -> when {
                            element.isString -> element.content
                            element.intOrNull != null -> element.int
                            element.longOrNull != null -> element.long
                            element.doubleOrNull != null -> element.double
                            element.booleanOrNull != null -> element.boolean
                            else -> element.content
                        }

                        is JsonArray -> element.map { deserializeJsonElement(it) }
                        is JsonObject -> deserializeJsonObject(element)
                    })
            }
        }
    }

    private fun deserializeJsonElement(element: JsonElement): Any? {
        return when (element) {
            is JsonNull -> null
            is JsonPrimitive -> when {
                element.isString -> element.content
                element.intOrNull != null -> element.int
                element.longOrNull != null -> element.long
                element.doubleOrNull != null -> element.double
                element.booleanOrNull != null -> element.boolean
                else -> element.content
            }

            is JsonArray -> element.map { deserializeJsonElement(it) }
            is JsonObject -> deserializeJsonObject(element)
        }
    }

    private fun deserializeJsonObject(jsonObject: JsonObject): Map<String, Any?> {
        return jsonObject.map { (key, value) ->
            key to deserializeJsonElement(value)
        }.toMap()
    }
}


class DefaultSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("DefaultValue", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {

        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonNull -> null
            is JsonPrimitive -> element.toString().removeSurrounding("\"")
            else -> element.toString()
        }
    }

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }
}

class ListSerializer : KSerializer<List<String?>?> {
    override val descriptor = PrimitiveSerialDescriptor("ListValue", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): List<String?>? {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonNull -> null
            else -> when (element) {
                is JsonArray -> element.map {
                    when (it) {
                        is JsonNull -> null
                        is JsonPrimitive -> it.toString().removeSurrounding("\"")
                        else -> it.toString()
                    }
                }

                else -> null
            }
        }
    }

    override fun serialize(encoder: Encoder, value: List<String?>?) {
        val jsonArray = JsonArray(value?.map { JsonPrimitive(it ?: "") } ?: emptyList())
        encoder.encodeSerializableValue(JsonElement.serializer(), jsonArray)
    }
}


private fun getTypeAccordingly(value: Any?): JsonElement {
    return when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is InitializePackingWorkFlow -> json.encodeToJsonElement(value)
        is CustomScreenAttributes -> json.encodeToJsonElement(value)
        is CustomField -> json.encodeToJsonElement(value)
        is TaskInfo -> json.encodeToJsonElement(value)
        is PickingAttribute -> json.encodeToJsonElement(value)
        is List<*> -> {
            // Handle lists differently
            val jsonArray = JsonArray(value.map { item ->
                getTypeAccordingly(item)
            })
            jsonArray
        }
        else -> json.encodeToJsonElement(value)
    }
}
