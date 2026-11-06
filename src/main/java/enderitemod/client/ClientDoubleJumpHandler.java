package enderitemod.client;

import enderitemod.Config;
import enderitemod.item.ModItems;
import enderitemod.network.DoubleJumpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientDoubleJumpHandler {
    private static boolean canDoubleJump = false;
    private static boolean hasReleasedJumpSinceAirborne = false;

    public static void init() {
        NeoForge.EVENT_BUS.addListener(ClientDoubleJumpHandler::onClientTick);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) {
            return;
        }

        // When touching ground, water, lava, or climbing: reset jump state
        if (player.onGround() || player.isInWater() || player.isInLava() || player.onClimbable()) {
            canDoubleJump = true;
            hasReleasedJumpSinceAirborne = false;
            return;
        }

        boolean isJumpDown = mc.options.keyJump.isDown();

        // While in the air, player must release the jump key before double jump is armed
        if (!isJumpDown) {
            hasReleasedJumpSinceAirborne = true;
        }

        // Double jump triggers ONLY after space was released while airborne and is now pressed again
        if (isJumpDown && hasReleasedJumpSinceAirborne && canDoubleJump) {
            if (!player.getAbilities().flying && !player.isFallFlying()) {
                if (Config.LEGGINGS_DOUBLE_JUMP_ENABLED.get() && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ENDERITE_LEGGINGS.get())) {
                    canDoubleJump = false;
                    hasReleasedJumpSinceAirborne = false;

                    double jumpVelocity = Config.LEGGINGS_DOUBLE_JUMP_VELOCITY.get();
                    player.setDeltaMovement(player.getDeltaMovement().x, jumpVelocity, player.getDeltaMovement().z);

                    // Sync velocity to server & reset fall distance
                    PacketDistributor.sendToServer(new DoubleJumpPayload());

                    // Visual/Audio feedback on client
                    player.level().addParticle(ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
                    player.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.5F, 1.8F);
                }
            }
        }
    }
}
