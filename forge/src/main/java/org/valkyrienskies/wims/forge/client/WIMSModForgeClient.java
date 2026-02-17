package org.valkyrienskies.wims.forge.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.valkyrienskies.wims.client.WIMSModClient;
import org.valkyrienskies.wims.forge.client.plugin.WIMSJourneyMapPlugin;

public class WIMSModForgeClient {
    public static void clientInit(FMLClientSetupEvent event) {
        // Put anything initialized on forge-side client here.
        WIMSModClient.InitClient();
    }

    @Mod.EventBusSubscriber(modid = "wims", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public final class ForgeClientTicks {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            WIMSJourneyMapPlugin.onClientTick();
        }
    }

}