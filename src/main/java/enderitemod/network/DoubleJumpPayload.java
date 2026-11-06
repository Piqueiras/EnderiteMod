package enderitemod.network;

import enderitemod.Config;
import enderitemod.EnderiteMod;
import enderitemod.item.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DoubleJumpPayload() implements CustomPacketPayload {
    public static final Type<DoubleJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EnderiteMod.MOD_ID, "double_jump"));

    public static final StreamCodec<ByteBuf, DoubleJumpPayload> STREAM_CODEC =
            StreamCodec.unit(new DoubleJumpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DoubleJumpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!Config.LEGGINGS_DOUBLE_JUMP_ENABLED.get()) {
                    return;
                }
                if (!player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ENDERITE_LEGGINGS.get())) {
                    return;
                }
                if (player.isSpectator() || player.isFallFlying()) {
                    return;
                }

                double jumpVelocity = Config.LEGGINGS_DOUBLE_JUMP_VELOCITY.get();
                player.setDeltaMovement(player.getDeltaMovement().x, jumpVelocity, player.getDeltaMovement().z);
                player.hurtMarked = true;
                player.resetFallDistance();

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.PORTAL,
                            player.getX(), player.getY(), player.getZ(),
                            20, 0.3, 0.1, 0.3, 0.1
                    );
                    serverLevel.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                            0.6F, 1.8F
                    );
                }
            }
        });
    }
}
