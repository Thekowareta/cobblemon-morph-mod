package com.team_aether.morph.network

import com.cobblemon.mod.common.entity.PoseType
import com.team_aether.morph.MorphDevMod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

data class MorphSyncPacket(
    val playerUuid: UUID,
    val speciesName: String,
    val additionalProperties: String,
    val width: Float,
    val height: Float,
    val initialPose: PoseType,
    val canFly: Boolean,
    val canWalk: Boolean,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<MorphSyncPacket> = TYPE

    companion object {
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(MorphDevMod.MOD_ID, "morph_sync")
        val TYPE: CustomPacketPayload.Type<MorphSyncPacket> = CustomPacketPayload.Type(ID)

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, MorphSyncPacket> = StreamCodec.of(
            { buf, p ->
                buf.writeUUID(p.playerUuid)
                buf.writeUtf(p.speciesName, 64)
                buf.writeUtf(p.additionalProperties, 256)
                buf.writeFloat(p.width)
                buf.writeFloat(p.height)
                buf.writeEnum(p.initialPose)
                buf.writeBoolean(p.canFly)
                buf.writeBoolean(p.canWalk)
            },
            { buf ->
                MorphSyncPacket(
                    buf.readUUID(),
                    buf.readUtf(64),
                    buf.readUtf(256),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readEnum(PoseType::class.java),
                    buf.readBoolean(),
                    buf.readBoolean(),
                )
            },
        )
    }
}

data class ClearMorphPacket(val dummy: Boolean = false) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<ClearMorphPacket> = TYPE

    companion object {
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(MorphDevMod.MOD_ID, "clear_morph")
        val TYPE: CustomPacketPayload.Type<ClearMorphPacket> = CustomPacketPayload.Type(ID)

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ClearMorphPacket> = StreamCodec.of(
            { _, _ -> },
            { ClearMorphPacket(false) },
        )
    }
}

data class SyncPoseS2CPacket(
    val playerUuid: UUID,
    val pose: PoseType,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<SyncPoseS2CPacket> = TYPE

    companion object {
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(MorphDevMod.MOD_ID, "sync_pose_s2c")
        val TYPE: CustomPacketPayload.Type<SyncPoseS2CPacket> = CustomPacketPayload.Type(ID)

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SyncPoseS2CPacket> = StreamCodec.of(
            { buf, p ->
                buf.writeUUID(p.playerUuid)
                buf.writeEnum(p.pose)
            },
            { buf ->
                SyncPoseS2CPacket(buf.readUUID(), buf.readEnum(PoseType::class.java))
            },
        )
    }
}

data class UpdatePoseC2SPacket(val pose: PoseType) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<UpdatePoseC2SPacket> = TYPE

    companion object {
        val ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(MorphDevMod.MOD_ID, "update_pose_c2s")
        val TYPE: CustomPacketPayload.Type<UpdatePoseC2SPacket> = CustomPacketPayload.Type(ID)

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, UpdatePoseC2SPacket> = StreamCodec.of(
            { buf, p -> buf.writeEnum(p.pose) },
            { buf -> UpdatePoseC2SPacket(buf.readEnum(PoseType::class.java)) },
        )
    }
}
