package enderitemod.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import enderitemod.Config;
import enderitemod.EnderiteMod;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class AddItemModifier extends LootModifier {
    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
                    .and(Codec.INT.optionalFieldOf("count", 1).forGetter(m -> m.count))
                    .apply(instance, AddItemModifier::new)
    );

    private final Item item;
    private final int count;

    public AddItemModifier(LootItemCondition[] conditionsIn, Item item, int count) {
        super(conditionsIn);
        this.item = item;
        this.count = count;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        float roll = context.getRandom().nextFloat();
        double chance = Config.END_CITY_LOOT_CHANCE.get();
        if (roll < chance) {
            int itemCount = Math.min(count, Config.END_CITY_LOOT_COUNT.get());
            generatedLoot.add(new ItemStack(item, itemCount));
            if (Config.LOG_ENDERITE_DETAILS.get()) {
                EnderiteMod.LOGGER.info("Enderite injected into End City treasure loot (roll: {}, chance: {})", roll, chance);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends LootModifier> codec() {
        return CODEC;
    }
}
