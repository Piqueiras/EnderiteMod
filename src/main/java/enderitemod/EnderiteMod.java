package enderitemod;

import com.mojang.logging.LogUtils;
import enderitemod.armor.EnderiteArmorHandler;
import enderitemod.client.ClientDoubleJumpHandler;
import enderitemod.item.ModArmorMaterials;
import enderitemod.item.ModItems;
import enderitemod.loot.ModLootModifiers;
import enderitemod.network.ModPackets;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(EnderiteMod.MOD_ID)
public class EnderiteMod {
    public static final String MOD_ID = "enderitemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnderiteMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModPackets::register);

        ModItems.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        EnderiteArmorHandler.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientDoubleJumpHandler.init();
        }

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Enderite Mod common setup initialized.");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.ENDERITE);
            event.accept(ModItems.ENDERITE_INGOT);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.ENDERITE_HELMET);
            event.accept(ModItems.ENDERITE_CHESTPLATE);
            event.accept(ModItems.ENDERITE_LEGGINGS);
            event.accept(ModItems.ENDERITE_BOOTS);
        }
    }
}
