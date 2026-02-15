package org.valkyrienskies.wims.fabric.client

import net.fabricmc.api.ClientModInitializer
import org.valkyrienskies.wims.client.WIMSModClient

/**
 * The fabric-side client initializer for the mod. Used for fabric-platform-specific code that runs on the client exclusively.
 */
class WIMSModFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Put anything initialized on fabric-side client here.
        WIMSModClient.initClient()
    }
}
