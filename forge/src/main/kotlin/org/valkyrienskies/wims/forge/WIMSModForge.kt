package org.valkyrienskies.wims.forge

import dev.architectury.platform.forge.EventBuses
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import org.valkyrienskies.wims.WIMSMod
import org.valkyrienskies.wims.forge.client.WIMSModForgeClient

@Mod(WIMSMod.MOD_ID)
class WIMSModForge {

    //Deferred Registries
    private val BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WIMSMod.MOD_ID)
    private val ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WIMSMod.MOD_ID)
    private val ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WIMSMod.MOD_ID)
    private val BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WIMSMod.MOD_ID)

    // Put RegistryObjects here:

    // end of RegistryObjects

    init {
        val modEventBus = FMLJavaModLoadingContext.get().modEventBus
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(WIMSMod.MOD_ID, modEventBus)

        modEventBus.addListener(::init)
        if (FMLEnvironment.dist.isClient) {
            modEventBus.addListener(WIMSModForgeClient.Companion::clientInit)
        }

        // Run our common setup.
        WIMSMod.init();
    }

    // Helper function, taken from VS2.
    private fun registerBlockAndItem(registryName: String, blockSupplier: () -> Block): RegistryObject<Block> {
        val blockRegistry = BLOCKS.register(registryName, blockSupplier)
        ITEMS.register(registryName) { BlockItem(blockRegistry.get(), Item.Properties()) }
        return blockRegistry
    }

    companion object {
        @JvmStatic
        fun init (event: FMLCommonSetupEvent) {
            // Put anything initialized on forge-side here.
        }
    }
}
