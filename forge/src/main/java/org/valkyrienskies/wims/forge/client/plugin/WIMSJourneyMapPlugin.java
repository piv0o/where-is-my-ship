package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
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

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.forge.WIMSModForge;

@journeymap.client.api.ClientPlugin
public class WIMSJourneyMapPlugin implements IClientPlugin {
    private IClientAPI jmAPI = null;

    // Forge listener reference
    private static WIMSJourneyMapPlugin INSTANCE;
    private WIMSClientEventListener eventListener;

    public ArrayList<ShipMapPacket> ships;
    public HashMap<String, ImageOverlay> shipOverlays = new HashMap<>();
    public HashMap<String, ResourceLocation> images = new HashMap<>();

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
//        jmAPI.removeAll(WIMSMod.MOD_ID);
        for (ShipMapPacket ship : getInstance().ships) {
            WIMSImageOverlay.updateImage(ship, jmAPI);

        }
    }

    private static void receiveShips(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        WIMSJourneyMapPlugin.INSTANCE.ships = ShipMapPacket.fromBuffer(buf);
    }

    private static void receiveImage(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        var image = ShipImagePacket.fromBuffer(buf);
//        WIMSModForge.LogInfo(String.format("image for %s: %s %s | %s %s", image.slug(), image.width(), image.height(), image.data().length, image.dataLength()));
        getInstance().images.put(image.slug(), WIMSImageOverlay.RegisterResource(image, WIMSImageOverlay.convertBytes(image)));
        if (getInstance().shipOverlays.containsKey(image.slug())) {
//            WIMSModForge.LogInfo(image.slug() + " updated Image");
//            var oldImage = getInstance().shipOverlays.get(image.slug()).getImage().getImage();
            getInstance().shipOverlays.get(image.slug()).setImage(WIMSImageOverlay.getShipImage(image.slug(), image.width(), image.height()));
            try {
                getInstance().jmAPI.show(getInstance().shipOverlays.get(image.slug()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//            if (oldImage != null) oldImage.close();
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



