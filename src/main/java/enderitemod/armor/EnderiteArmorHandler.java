package enderitemod.armor;

import enderitemod.Config;
import enderitemod.EnderiteMod;
import enderitemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnderiteArmorHandler {
    private static final ResourceLocation CHESTPLATE_HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(EnderiteMod.MOD_ID, "enderite_chestplate_health");
    private static final ResourceLocation HELMET_BLOCK_RANGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(EnderiteMod.MOD_ID, "enderite_helmet_block_range");

    // Tracks remaining ticks until next instability teleport per player UUID
    private static final Map<UUID, Integer> INSTABILITY_TIMERS = new ConcurrentHashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EnderiteArmorHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(EnderiteArmorHandler::onLivingFall);
        NeoForge.EVENT_BUS.addListener(EnderiteArmorHandler::onPlayerLoggedOut);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        INSTABILITY_TIMERS.remove(event.getEntity().getUUID());
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            handleArmorEffects(player);
        }
    }

    private static void handleArmorEffects(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        int pieceCount = 0;
        if (head.is(ModItems.ENDERITE_HELMET.get())) pieceCount++;
        if (chest.is(ModItems.ENDERITE_CHESTPLATE.get())) pieceCount++;
        if (legs.is(ModItems.ENDERITE_LEGGINGS.get())) pieceCount++;
        if (feet.is(ModItems.ENDERITE_BOOTS.get())) pieceCount++;

        // 1. Helmet: Night Vision & +2 Block Place Range
        if (head.is(ModItems.ENDERITE_HELMET.get()) && Config.HELMET_NIGHT_VISION_ENABLED.get()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, false, false, false));
        }

        AttributeInstance blockRangeAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockRangeAttr != null) {
            double rangeBonus = Config.HELMET_BLOCK_RANGE_BONUS.get();
            if (head.is(ModItems.ENDERITE_HELMET.get()) && rangeBonus > 0) {
                if (!blockRangeAttr.hasModifier(HELMET_BLOCK_RANGE_MODIFIER_ID)) {
                    blockRangeAttr.addTransientModifier(new AttributeModifier(
                            HELMET_BLOCK_RANGE_MODIFIER_ID,
                            rangeBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    ));
                }
            } else {
                if (blockRangeAttr.hasModifier(HELMET_BLOCK_RANGE_MODIFIER_ID)) {
                    blockRangeAttr.removeModifier(HELMET_BLOCK_RANGE_MODIFIER_ID);
                }
            }
        }

        // 2. Chestplate: Health Bonus & Regeneration
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double healthBonus = Config.CHESTPLATE_HEALTH_BONUS.get();
            if (chest.is(ModItems.ENDERITE_CHESTPLATE.get()) && healthBonus > 0) {
                if (!maxHealthAttr.hasModifier(CHESTPLATE_HEALTH_MODIFIER_ID)) {
                    maxHealthAttr.addTransientModifier(new AttributeModifier(
                            CHESTPLATE_HEALTH_MODIFIER_ID,
                            healthBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    ));
                }
            } else {
                if (maxHealthAttr.hasModifier(CHESTPLATE_HEALTH_MODIFIER_ID)) {
                    maxHealthAttr.removeModifier(CHESTPLATE_HEALTH_MODIFIER_ID);
                    if (player.getHealth() > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
                }
            }
        }

        if (chest.is(ModItems.ENDERITE_CHESTPLATE.get()) && Config.CHESTPLATE_REGEN_ENABLED.get()) {
            if (player.tickCount % 40 == 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        80,
                        Config.CHESTPLATE_REGEN_AMPLIFIER.get(),
                        false,
                        false,
                        true
                ));
            }
        }

        // 3. Boots: Extra Speed
        if (feet.is(ModItems.ENDERITE_BOOTS.get()) && Config.BOOTS_SPEED_ENABLED.get()) {
            if (player.tickCount % 40 == 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        80,
                        Config.BOOTS_SPEED_AMPLIFIER.get(),
                        false,
                        false,
                        true
                ));
            }
        }

        // 4. Instability: Interval-based countdown when wearing more than 1 piece (excessPieces = pieceCount - 1)
        int excessPieces = pieceCount - 1;
        UUID uuid = player.getUUID();
        if (Config.INSTABILITY_ENABLED.get() && excessPieces > 0) {
            Integer timer = INSTABILITY_TIMERS.get(uuid);
            if (timer == null) {
                // Initial countdown setup when entering instability
                INSTABILITY_TIMERS.put(uuid, getRandomIntervalTicks(player.getRandom(), excessPieces));
            } else if (timer <= 0) {
                // Interval elapsed -> trigger teleport!
                double baseMinDist = Config.INSTABILITY_BASE_MIN_DISTANCE.get();
                double baseMaxDist = Config.INSTABILITY_BASE_MAX_DISTANCE.get();
                double distMultiplier = Config.INSTABILITY_DISTANCE_MULTIPLIER_PER_PIECE.get();

                double scale = Math.pow(distMultiplier, excessPieces - 1);
                double minDist = baseMinDist * scale;
                double maxDist = baseMaxDist * scale;

                triggerInstabilityTeleport(player, pieceCount, excessPieces, minDist, maxDist);

                // Re-arm countdown with next random interval
                INSTABILITY_TIMERS.put(uuid, getRandomIntervalTicks(player.getRandom(), excessPieces));
            } else {
                INSTABILITY_TIMERS.put(uuid, timer - 1);
            }
        } else {
            // Player is stable or feature is disabled
            INSTABILITY_TIMERS.remove(uuid);
        }
    }

    private static int getRandomIntervalTicks(RandomSource random, int excessPieces) {
        int baseMinSec = Config.INSTABILITY_BASE_MIN_INTERVAL_SECONDS.get();
        int baseMaxSec = Config.INSTABILITY_BASE_MAX_INTERVAL_SECONDS.get();
        double multiplier = Config.INSTABILITY_INTERVAL_MULTIPLIER_PER_PIECE.get();

        double scale = Math.pow(multiplier, excessPieces - 1);
        int minSec = Math.max(2, (int) Math.round(baseMinSec * scale));
        int maxSec = Math.max(minSec + 1, (int) Math.round(baseMaxSec * scale));

        int chosenSeconds = minSec + random.nextInt(maxSec - minSec + 1);
        return chosenSeconds * 20; // 20 ticks per second
    }

    private static void triggerInstabilityTeleport(ServerPlayer player, int pieceCount, int excessPieces, double minDist, double maxDist) {
        ServerLevel level = player.serverLevel();
        RandomSource random = player.getRandom();

        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double dist = minDist + random.nextDouble() * (maxDist - minDist);
            double targetX = player.getX() + Math.cos(angle) * dist;
            double targetZ = player.getZ() + Math.sin(angle) * dist;

            BlockPos targetPos = BlockPos.containing(targetX, player.getY(), targetZ);
            if (!level.hasChunkAt(targetPos)) {
                continue;
            }

            int startY = Mth.clamp(targetPos.getY() + 4, level.getMinBuildHeight() + 2, level.getMinBuildHeight() + level.getLogicalHeight() - 2);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(targetPos.getX(), startY, targetPos.getZ());

            boolean foundGround = false;
            for (int dy = 0; dy < 16; dy++) {
                BlockState floorState = level.getBlockState(cursor);
                BlockPos above1 = cursor.above();
                BlockPos above2 = cursor.above(2);

                if (floorState.blocksMotion() && !floorState.is(Blocks.LAVA) && !floorState.is(Blocks.FIRE)) {
                    BlockState stateAbove1 = level.getBlockState(above1);
                    BlockState stateAbove2 = level.getBlockState(above2);

                    if (!stateAbove1.blocksMotion() && !stateAbove2.blocksMotion() &&
                            !stateAbove1.liquid() && !stateAbove2.liquid()) {
                        foundGround = true;
                        break;
                    }
                }
                cursor.move(Direction.DOWN);
                if (cursor.getY() <= level.getMinBuildHeight()) {
                    break;
                }
            }

            if (foundGround) {
                double destX = cursor.getX() + 0.5;
                double destY = cursor.getY() + 1.0;
                double destZ = cursor.getZ() + 0.5;

                Vec3 oldPos = player.position();

                if (player.isPassenger()) {
                    player.stopRiding();
                }

                // Explicit server teleportation updating server & client position smoothly
                player.teleportTo(level, destX, destY, destZ, Set.of(), player.getYRot(), player.getXRot());
                player.resetFallDistance();

                level.gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(player));

                if (Config.INSTABILITY_PLAY_SOUND.get()) {
                    level.playSound(null, oldPos.x, oldPos.y, oldPos.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.playSound(null, destX, destY, destZ, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                level.sendParticles(ParticleTypes.PORTAL, oldPos.x, oldPos.y + 1.0, oldPos.z, 36, 0.4, 0.6, 0.4, 0.1);
                level.sendParticles(ParticleTypes.PORTAL, destX, destY + 1.0, destZ, 36, 0.4, 0.6, 0.4, 0.1);

                if (Config.LOG_ENDERITE_DETAILS.get()) {
                    EnderiteMod.LOGGER.info("Player {} teleported {} blocks away (pieces: {}, excess over one: {})",
                            player.getScoreboardName(), String.format("%.1f", dist), pieceCount, excessPieces);
                }
                break;
            }
        }
    }

    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (Config.LEGGINGS_NO_FALL_DAMAGE.get() && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ENDERITE_LEGGINGS.get())) {
                event.setCanceled(true);
                event.setDistance(0.0F);
            }
        }
    }
}
