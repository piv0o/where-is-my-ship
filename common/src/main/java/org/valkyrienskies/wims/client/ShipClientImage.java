package org.valkyrienskies.wims.client;

import net.minecraft.resources.ResourceLocation;

public record ShipClientImage(
        ResourceLocation resource,
        int width,
        int height
) {

}
