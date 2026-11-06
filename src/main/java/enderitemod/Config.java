package enderitemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Loot settings
    public static final ModConfigSpec.DoubleValue END_CITY_LOOT_CHANCE;
    public static final ModConfigSpec.IntValue END_CITY_LOOT_COUNT;
    public static final ModConfigSpec.BooleanValue LOG_ENDERITE_DETAILS;

    // Helmet effects
    public static final ModConfigSpec.BooleanValue HELMET_NIGHT_VISION_ENABLED;
    public static final ModConfigSpec.DoubleValue HELMET_BLOCK_RANGE_BONUS;

    // Chestplate effects
    public static final ModConfigSpec.DoubleValue CHESTPLATE_HEALTH_BONUS;
    public static final ModConfigSpec.BooleanValue CHESTPLATE_REGEN_ENABLED;
    public static final ModConfigSpec.IntValue CHESTPLATE_REGEN_AMPLIFIER;

    // Leggings effects
    public static final ModConfigSpec.BooleanValue LEGGINGS_DOUBLE_JUMP_ENABLED;
    public static final ModConfigSpec.DoubleValue LEGGINGS_DOUBLE_JUMP_VELOCITY;
    public static final ModConfigSpec.BooleanValue LEGGINGS_NO_FALL_DAMAGE;

    // Boots effects
    public static final ModConfigSpec.BooleanValue BOOTS_SPEED_ENABLED;
    public static final ModConfigSpec.IntValue BOOTS_SPEED_AMPLIFIER;

    // Instability settings (interval countdown & distance scaling with pieces over 1)
    public static final ModConfigSpec.BooleanValue INSTABILITY_ENABLED;
    public static final ModConfigSpec.IntValue INSTABILITY_BASE_MIN_INTERVAL_SECONDS;
    public static final ModConfigSpec.IntValue INSTABILITY_BASE_MAX_INTERVAL_SECONDS;
    public static final ModConfigSpec.DoubleValue INSTABILITY_INTERVAL_MULTIPLIER_PER_PIECE;
    public static final ModConfigSpec.DoubleValue INSTABILITY_BASE_MIN_DISTANCE;
    public static final ModConfigSpec.DoubleValue INSTABILITY_BASE_MAX_DISTANCE;
    public static final ModConfigSpec.DoubleValue INSTABILITY_DISTANCE_MULTIPLIER_PER_PIECE;
    public static final ModConfigSpec.BooleanValue INSTABILITY_PLAY_SOUND;

    static {
        BUILDER.push("loot");
        END_CITY_LOOT_CHANCE = BUILDER
                .comment("Chance of Enderite spawning in End City treasure chests (0.0 to 1.0).")
                .defineInRange("endCityLootChance", 0.35, 0.0, 1.0);
        END_CITY_LOOT_COUNT = BUILDER
                .comment("Maximum count of Enderite items per roll in End City treasure chests.")
                .defineInRange("endCityLootCount", 1, 1, 64);
        LOG_ENDERITE_DETAILS = BUILDER
                .comment("Whether to enable Enderite-related logging in the console.")
                .define("enableLogging", true);
        BUILDER.pop();

        BUILDER.push("armor_helmet");
        HELMET_NIGHT_VISION_ENABLED = BUILDER
                .comment("Whether the Enderite Helmet grants Night Vision.")
                .define("nightVisionEnabled", true);
        HELMET_BLOCK_RANGE_BONUS = BUILDER
                .comment("Extra block place and interaction reach distance granted by the Enderite Helmet (e.g. 2.0 = +2 blocks).")
                .defineInRange("blockRangeBonus", 2.0, 0.0, 64.0);
        BUILDER.pop();

        BUILDER.push("armor_chestplate");
        CHESTPLATE_HEALTH_BONUS = BUILDER
                .comment("Extra max health points granted by the Enderite Chestplate (4.0 = 2 hearts). Set to 0.0 to disable.")
                .defineInRange("healthBonus", 4.0, 0.0, 100.0);
        CHESTPLATE_REGEN_ENABLED = BUILDER
                .comment("Whether the Enderite Chestplate grants Regeneration.")
                .define("regenEnabled", true);
        CHESTPLATE_REGEN_AMPLIFIER = BUILDER
                .comment("Regeneration amplifier (0 = Regen I, 1 = Regen II).")
                .defineInRange("regenAmplifier", 0, 0, 10);
        BUILDER.pop();

        BUILDER.push("armor_leggings");
        LEGGINGS_DOUBLE_JUMP_ENABLED = BUILDER
                .comment("Whether the Enderite Leggings allow a mid-air double jump.")
                .define("doubleJumpEnabled", true);
        LEGGINGS_DOUBLE_JUMP_VELOCITY = BUILDER
                .comment("Upward velocity applied on double jump.")
                .defineInRange("doubleJumpVelocity", 0.45, 0.1, 5.0);
        LEGGINGS_NO_FALL_DAMAGE = BUILDER
                .comment("Whether the Enderite Leggings negate all fall damage.")
                .define("noFallDamage", true);
        BUILDER.pop();

        BUILDER.push("armor_boots");
        BOOTS_SPEED_ENABLED = BUILDER
                .comment("Whether the Enderite Boots grant Speed.")
                .define("speedEnabled", true);
        BOOTS_SPEED_AMPLIFIER = BUILDER
                .comment("Speed amplifier (0 = Speed I, 1 = Speed II).")
                .defineInRange("speedAmplifier", 0, 0, 10);
        BUILDER.pop();

        BUILDER.push("armor_instability");
        INSTABILITY_ENABLED = BUILDER
                .comment("Whether wearing multiple pieces of Enderite armor (more than 1) causes instability (random Chorus Fruit teleports).")
                .define("enabled", true);
        INSTABILITY_BASE_MIN_INTERVAL_SECONDS = BUILDER
                .comment("Minimum seconds between instability teleports with 2 pieces equipped (1 piece over 1). Prevents teleports happening too close together.")
                .defineInRange("baseMinIntervalSeconds", 15, 1, 3600);
        INSTABILITY_BASE_MAX_INTERVAL_SECONDS = BUILDER
                .comment("Maximum seconds between instability teleports with 2 pieces equipped (1 piece over 1).")
                .defineInRange("baseMaxIntervalSeconds", 45, 2, 3600);
        INSTABILITY_INTERVAL_MULTIPLIER_PER_PIECE = BUILDER
                .comment("Interval reduction multiplier for each additional piece equipped beyond 2 pieces (e.g. 0.6 shortens intervals by 40% per extra piece).")
                .defineInRange("intervalMultiplierPerPiece", 0.6, 0.1, 1.0);
        INSTABILITY_BASE_MIN_DISTANCE = BUILDER
                .comment("Base minimum teleport distance in blocks with 2 pieces equipped.")
                .defineInRange("baseMinDistance", 6.0, 1.0, 64.0);
        INSTABILITY_BASE_MAX_DISTANCE = BUILDER
                .comment("Base maximum teleport distance in blocks with 2 pieces equipped.")
                .defineInRange("baseMaxDistance", 14.0, 2.0, 128.0);
        INSTABILITY_DISTANCE_MULTIPLIER_PER_PIECE = BUILDER
                .comment("Distance multiplier for each additional piece equipped beyond 2 pieces.")
                .defineInRange("distanceMultiplierPerPiece", 1.6, 1.0, 10.0);
        INSTABILITY_PLAY_SOUND = BUILDER
                .comment("Whether to play the Chorus Fruit teleport sound when instability triggers.")
                .define("playSound", true);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
