package org.valkyrienskies.wims.forge.client.plugin;

import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.event.WaypointEvent;
import journeymap.client.api.event.RegistryEvent;
import journeymap.client.api.display.DisplayType;

import static journeymap.client.api.event.ClientEvent.Type.DEATH_WAYPOINT;
import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STARTED;
import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STOPPED;
import static journeymap.client.api.event.ClientEvent.Type.REGISTRY;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.forge.WIMSModForge;

@journeymap.client.api.ClientPlugin
public class WIMSJourneyMapPlugin implements IClientPlugin {
    private IClientAPI jmAPI = null;

    // Forge listener reference
    private static WIMSJourneyMapPlugin INSTANCE;
    private WIMSClientEventListener eventListener;

    public WIMSJourneyMapPlugin() {
        INSTANCE = this;
    }

    public static WIMSJourneyMapPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void initialize(final IClientAPI jmAPI) {
        this.jmAPI = jmAPI;

        eventListener = new WIMSClientEventListener(jmAPI);
        MinecraftForge.EVENT_BUS.register(eventListener);
        this.jmAPI.subscribe(getModId(), EnumSet.of(DEATH_WAYPOINT, MAPPING_STARTED, MAPPING_STOPPED, REGISTRY));
    }

    @Override
    public String getModId() {
        return WIMSMod.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event) {
        try {
            switch (event.type) {
                case MAPPING_STARTED:
                    onMappingStarted(event);
                    break;

                case MAPPING_STOPPED:
                    onMappingStopped(event);
                    break;
                case WAYPOINT:
                    onWaypointEvent((WaypointEvent) event);
                    break;
                case DEATH_WAYPOINT:
                    onDeathpoint((DeathWaypointEvent) event);
                    break;
                case REGISTRY:
                    RegistryEvent registryEvent = (RegistryEvent) event;
                    switch (registryEvent.getRegistryType()) {
                        case OPTIONS:
//                            this.clientProperties = new ClientProperties();
                            break;
                        case INFO_SLOT:
                            ((RegistryEvent.InfoSlotRegistryEvent) registryEvent)
                                    .register(getModId(), "Current Millis", 1000, () -> "Millis: " + System.currentTimeMillis());
                            ((RegistryEvent.InfoSlotRegistryEvent) registryEvent)
                                    .register(getModId(), "Current Ticks", 10, WIMSJourneyMapPlugin::getTicks);
                            break;
                    }
                    break;
            }
        } catch (Throwable t) {
//            WIMSModForge.LOGGER.error(t.getMessage(), t);
        }
    }

    private static String getTicks() {
        return "Ticks: " + Minecraft.getInstance().gui.getGuiTicks();
    }

    private void onDeathpoint(DeathWaypointEvent event) {
        // Create a bunch of random Image Overlays around the player
        if (jmAPI.playerAccepts(getModId(), DisplayType.Image))
        {
            BlockPos pos = Minecraft.getInstance().player.blockPosition();
            SampleImageOverlayFactory.create(jmAPI, pos, 5, 256, 128);
        }

        // Create a bunch of random Marker Overlays around the player
        if (jmAPI.playerAccepts(getModId(), DisplayType.Marker))
        {
            net.minecraft.core.BlockPos pos = Minecraft.getInstance().player.blockPosition();
//            SampleMarkerOverlayFactory.create(jmAPI, pos, 64, 256);
        }

        // Create a waypoint for the player's bed location.  The ForgeEventListener
        // will keep it updated if the player sleeps elsewhere.
        if (jmAPI.playerAccepts(getModId(), DisplayType.Waypoint))
        {
            BlockPos pos = Minecraft.getInstance().player.getSleepingPos().orElse(new BlockPos(0, 0, 0));
//            SampleWaypointFactory.createBedWaypoint(jmAPI, pos, event.dimension);
        }

        // Create some random complex polygon overlays
        if (jmAPI.playerAccepts(getModId(), DisplayType.Polygon))
        {
            BlockPos pos = Minecraft.getInstance().player.blockPosition();
//            SampleComplexPolygonOverlayFactory.create(jmAPI, pos, event.dimension, 256);
        }
    }

    private void onWaypointEvent(WaypointEvent event) {

    }

    private void onMappingStopped(ClientEvent event) {

    }

    private void onMappingStarted(ClientEvent event) {

    }
}



