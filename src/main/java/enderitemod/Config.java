package enderitemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue END_CITY_LOOT_CHANCE = BUILDER
            .comment("Chance of Enderite spawning in End City treasure chests (0.0 to 1.0).")
            .defineInRange("endCityLootChance", 0.35, 0.0, 1.0);

    public static final ModConfigSpec.IntValue END_CITY_LOOT_COUNT = BUILDER
            .comment("Maximum count of Enderite items per roll in End City treasure chests.")
            .defineInRange("endCityLootCount", 1, 1, 64);

    public static final ModConfigSpec.BooleanValue LOG_ENDERITE_DETAILS = BUILDER
            .comment("Whether to enable Enderite-related logging.")
            .define("enableLogging", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
