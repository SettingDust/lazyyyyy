package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HashCodeSerializer : KSerializer<HashCode> {
    override val descriptor = PrimitiveSerialDescriptor("HashCode", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): HashCode {
        return HashCode.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: HashCode) {
        encoder.encodeString(value.toString())
    }
}