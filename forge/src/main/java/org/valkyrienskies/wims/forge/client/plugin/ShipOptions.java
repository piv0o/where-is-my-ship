package org.valkyrienskies.wims.forge.client.plugin;

import journeymap.client.api.option.KeyedEnum;

public enum ShipOptions implements KeyedEnum {
    NEVER("Never"),
    FULLSCREEN_ONLY("Fullscreen Only"),
    MINIMAP_ONLY("Minimap Only"),
    ALWAYS("Always");


    private final String key;

    ShipOptions(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return key;
    }
}
