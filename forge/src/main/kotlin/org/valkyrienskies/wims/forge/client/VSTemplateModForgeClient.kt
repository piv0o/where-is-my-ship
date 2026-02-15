package org.valkyrienskies.wims.forge.client

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.valkyrienskies.wims.client.WIMSModClient

class WIMSModForgeClient {
    companion object {
        @JvmStatic
        fun clientInit(event: FMLClientSetupEvent) {
            // Put anything initialized on forge-side client here.
            WIMSModClient.initClient()
        }
    }
}
