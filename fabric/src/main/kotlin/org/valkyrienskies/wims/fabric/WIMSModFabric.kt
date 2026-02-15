package org.valkyrienskies.wims.fabric

import net.fabricmc.api.ModInitializer
import org.valkyrienskies.wims.WIMSMod

/**
 * The fabric-side initializer for the mod. Used for fabric-platform-specific code.
 */
class WIMSModFabric : ModInitializer {
    override fun onInitialize() {
        // Put anything initialized on fabric-side here, such as platform-specific registries.
        WIMSMod.init()
    }
}
