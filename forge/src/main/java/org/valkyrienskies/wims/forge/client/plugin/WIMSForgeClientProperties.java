package org.valkyrienskies.wims.forge.client.plugin;

import journeymap.client.api.option.*;
import org.valkyrienskies.wims.WIMSMod;

public class WIMSForgeClientProperties {
    private OptionCategory category = new OptionCategory(WIMSMod.MOD_ID, "Where is My Ship");

    //TODO: Outline should be sepeate for Minimap and Fullscreen
    public BooleanOption shipsHaveOutline;
    public EnumOption<ShipOptions> shipsShouldRender;
    public EnumOption<ShipOptions> shipsShouldHaveLabels;
    public IntegerOption MinMassForLabel;

    public WIMSForgeClientProperties() {
        this.shipsHaveOutline = new BooleanOption(category, "shipsHaveOutline", "Should Ships have Outlines", false);
        this.shipsShouldRender = new EnumOption<>(category, "shipsShouldRender", "Should Ships Render", ShipOptions.ALWAYS);
        this.shipsShouldHaveLabels = new EnumOption<>(category, "shipsShouldHaveLabels", "Should Ships Have Labels", ShipOptions.ALWAYS);
        this.MinMassForLabel = new IntegerOption(category, "MinMassForLabel", "Minimum Mass (in kg) to show Label ", 0, 0, 10000);
    }
}

