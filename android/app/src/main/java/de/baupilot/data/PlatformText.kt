package de.baupilot.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable(with = PlatformTextSerializer::class)
data class PlatformText(val ios: String?, val android: String?) {
    val value: String? get() = android
}

object PlatformTextSerializer : KSerializer<PlatformText> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PlatformText")

    override fun deserialize(decoder: Decoder): PlatformText {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("PlatformText can only be decoded from JSON")
        return when (val element = input.decodeJsonElement()) {
            is JsonObject -> {
                if (element.keys != setOf("ios", "android")) {
                    throw SerializationException("PlatformText object must have exactly the keys ios and android, got ${element.keys}")
                }
                PlatformText(
                    ios = element.platformValue("ios"),
                    android = element.platformValue("android"),
                )
            }
            is JsonPrimitive ->
                if (element.isString) PlatformText(element.content, element.content)
                else throw SerializationException("PlatformText must be a string or an object, got $element")
            else -> throw SerializationException("PlatformText must be a string or an object, got $element")
        }
    }

    override fun serialize(encoder: Encoder, value: PlatformText) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("PlatformText can only be encoded to JSON")
        output.encodeJsonElement(
            JsonObject(
                mapOf(
                    "ios" to (value.ios?.let(::JsonPrimitive) ?: JsonNull),
                    "android" to (value.android?.let(::JsonPrimitive) ?: JsonNull),
                ),
            ),
        )
    }

    private fun JsonObject.platformValue(key: String): String? {
        val entry = getValue(key)
        if (entry is JsonNull) return null
        val primitive = entry as? JsonPrimitive
        if (primitive == null || !primitive.isString) {
            throw SerializationException("PlatformText.$key must be a string or null, got $entry")
        }
        return primitive.contentOrNull
    }
}
