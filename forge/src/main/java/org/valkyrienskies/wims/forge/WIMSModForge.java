package org.valkyrienskies.wims.forge;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.forge.client.WIMSModForgeClient;


@Mod(WIMSMod.MOD_ID)
public class WIMSModForge {

    // Deferred Registries
    private final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WIMSMod.MOD_ID);
    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WIMSMod.MOD_ID);
    private final DeferredRegister<?> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WIMSMod.MOD_ID);
    private final DeferredRegister<?> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WIMSMod.MOD_ID);

    private static final Logger LOGGER = LogUtils.getLogger();

    // Put RegistryObjects here:

    // end of RegistryObjects

    public WIMSModForge() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(WIMSMod.MOD_ID, modEventBus);

        modEventBus.addListener(this::init);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(WIMSModForgeClient::clientInit);
        }

        // Run our common setup.
        WIMSMod.Init();
    }

    // Helper function, taken from VS2.
    private RegistryObject<Block> registerBlockAndItem(String registryName, java.util.function.Supplier<? extends Block> blockSupplier) {
        RegistryObject<Block> blockRegistry = BLOCKS.register(registryName, blockSupplier);
        ITEMS.register(registryName, () -> new BlockItem(blockRegistry.get(), new Item.Properties()));
        return blockRegistry;
    }

    public static class Companion {
        @SuppressWarnings("unused")
        public static void init(FMLCommonSetupEvent event) {
            // Put anything initialized on forge-side here.
        }
    }

    public static void LogInfo(String msg){
        LOGGER.info(msg);
    }

    public static void LogError(String msg){
        LOGGER.error(msg);
    }

    public static void LogWarn(String msg){
        LOGGER.warn(msg);
    }

    private void init(FMLCommonSetupEvent event) {
        Companion.init(event);
    }
}