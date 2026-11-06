package enderitemod.item;

import enderitemod.EnderiteMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, EnderiteMod.MOD_ID);

    public static final Holder<ArmorMaterial> ENDERITE_ARMOR_MATERIAL = ARMOR_MATERIALS.register("enderite",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), attribute -> {
                        attribute.put(ArmorItem.Type.BOOTS, 3);
                        attribute.put(ArmorItem.Type.LEGGINGS, 6);
                        attribute.put(ArmorItem.Type.CHESTPLATE, 10);
                        attribute.put(ArmorItem.Type.HELMET, 3);
                        attribute.put(ArmorItem.Type.BODY, 6);
                    }),
                    18,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(ModItems.ENDERITE_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(EnderiteMod.MOD_ID, "enderite"))),
                    3.5f,
                    0.1f
            ));

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}
