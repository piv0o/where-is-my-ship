package org.valkyrienskies.wims.forge.client;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.valkyrienskies.wims.client.WIMSModClient;

public class WIMSModForgeClient {
    public static void clientInit(FMLClientSetupEvent event) {
        // Put anything initialized on forge-side client here.
        WIMSModClient.InitClient();
    }
}