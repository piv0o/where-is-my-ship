package org.valkyrienskies.wims.forge.client.plugin;

import dev.architectury.networking.NetworkManager;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.IClientAPI;

import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STARTED;
import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STOPPED;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;

@journeymap.client.api.ClientPlugin
public class WIMSJourneyMapPlugin implements IClientPlugin {
    private IClientAPI jmAPI = null;

    // Forge listener reference
    private static WIMSJourneyMapPlugin INSTANCE;
    private WIMSClientEventListener eventListener;

    public ArrayList<ShipMapPacket> ships;
    public HashMap<String, ImageOverlay> shipOverlays = new HashMap<>();
    public HashMap<String, ResourceLocation> images = new HashMap<>();
    public ArrayList<String> OverlaysToRemove = new ArrayList<>();

    private boolean isMappingStarted;

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
        this.jmAPI.subscribe(getModId(), EnumSet.of(MAPPING_STARTED, MAPPING_STOPPED));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_PACKET_ID, WIMSJourneyMapPlugin::receiveShips);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_IMAGE_PACKET_ID, WIMSJourneyMapPlugin::receiveImage);
    }


    public static void onClientTick() {
        if (getInstance().ships == null || !getInstance().isMappingStarted) return;
        var jmAPI = getInstance().jmAPI;
        for (ShipMapPacket ship : getInstance().ships) {
            WIMSImageOverlay.updateImage(ship, jmAPI);

        }
    }

    private static void receiveShips(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        WIMSJourneyMapPlugin.INSTANCE.ships = ShipMapPacket.fromBuffer(buf);
    }

    private static void receiveImage(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ShipImagePacket image = ShipImagePacket.fromBuffer(buf);
        getInstance().images.put(image.slug(), WIMSImageOverlay.RegisterResource(image, WIMSImageOverlay.convertBytes(image)));
        if (getInstance().shipOverlays.containsKey(image.slug())) {
                getInstance().jmAPI.remove(getInstance().shipOverlays.get(image.slug()));
                getInstance().shipOverlays.remove(image.slug());
        }
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
            }
        } catch (Throwable t) {
            WIMSMod.LogError(t.getMessage());
        }
    }


    private void onMappingStopped(ClientEvent event) {
        this.isMappingStarted = false;
    }

    private void onMappingStarted(ClientEvent event) {
        this.isMappingStarted = true;
    }
}



