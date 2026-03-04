package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.IClientAPI;

import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STARTED;
import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STOPPED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;

import journeymap.client.api.util.UIState;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.texture.Texture;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.client.ShipClientImage;

@journeymap.client.api.ClientPlugin
public class WIMSJourneyMapPlugin implements IClientPlugin {
    public float partialTick;
    private IClientAPI jmAPI = null;

    // Forge listener reference
    private static WIMSJourneyMapPlugin INSTANCE;

    public ArrayList<ShipMapPacket> ships;
    public HashMap<String, ShipClientImage> images = new HashMap<>();

    private boolean isMappingStarted;

    public WIMSJourneyMapPlugin() {
        INSTANCE = this;
    }

    public static WIMSJourneyMapPlugin getInstance() {
        return INSTANCE;
    }

    // grabbing from JourneyMapFullscreenMixin
    // heavily based on create train map renderer
    public static void OnFullscreenRender(GuiGraphics graphics, Fullscreen screen, double x, double z, int mX, int mY, FullMapProperties fullMapProperties) {
        UIState state = screen.getUiState();
        if (state == null) return;
        if (state.ui != Context.UI.Fullscreen) return;
        if (!state.active) return;

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        double scale = state.blockSize;

        PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(window.getScreenWidth() / 2.0f, window.getScreenHeight() / 2.0f, 0);
        pose.scale((float) scale, (float) scale, 1);
        pose.translate(-x, -z, 0);

        float mouseX = mX - screen.width / 2.0f;
        float mouseY = mY - screen.height / 2.0f;
        mouseX /= (float) scale;
        mouseY /= (float) scale;
        mouseX += (float) x;
        mouseY += (float) z;

        Rect2i bounds =
                new Rect2i(Mth.floor(-screen.width / 2.0f / scale + x), Mth.floor(-screen.height / 2.0f / scale + z),
                        Mth.floor(screen.width / scale), Mth.floor(screen.height / scale));

        ShipMapUtility.drawShips(graphics, (int) Math.floor(mouseX), (int) Math.floor(mouseY), 1f / scale, bounds);
        tickShipVelocities();
        pose.popPose();
    }

    public static void OnMinimapRender(GuiGraphics graphics, MiniMap screen, double x, double z, GridRenderer gridRenderer, MiniMapProperties miniMapProperties) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Window window = mc.getWindow();
            PoseStack pose = graphics.pose();
            MultiBufferSource.BufferSource buffer = graphics.bufferSource();
            var scale = Math.pow((double)2.0F, (double) miniMapProperties.zoomLevel.get());
            if (mc.player != null) {
                WIMSMod.LogInfo("Minimap X: %s Z: %s SCALE: %s", x, z, scale);
                ShipMapUtility.drawMiniShips(graphics, null, null, scale, null, gridRenderer, buffer);
            }
        } catch (Exception e) {
            WIMSMod.LogError(e.getMessage());
            WIMSMod.LogError(Arrays.toString(e.getStackTrace()));
        }

    }

    private static void tickShipVelocities() {
//        getInstance().ships.replaceAll(ship -> ship.tickVelocity(getInstance().partialTick * (1f/20f)));
//        getInstance().partialTick = 0;
    }

    @Override
    public void initialize(final IClientAPI jmAPI) {
        this.jmAPI = jmAPI;

        this.jmAPI.subscribe(getModId(), EnumSet.of(MAPPING_STARTED, MAPPING_STOPPED));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_PACKET_ID, WIMSJourneyMapPlugin::receiveShips);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_IMAGE_PACKET_ID, WIMSJourneyMapPlugin::receiveImage);
    }


    public static void onClientTick() {
        if (getInstance().ships == null || !getInstance().isMappingStarted) return;
        var jmAPI = getInstance().jmAPI;
        for (ShipMapPacket ship : getInstance().ships) {

        }
    }

    private static void receiveShips(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        WIMSJourneyMapPlugin.INSTANCE.ships = ShipMapPacket.fromBuffer(buf);
    }

    private static void receiveImage(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ShipImagePacket image = ShipImagePacket.fromBuffer(buf);
        ShipMapUtility.RegisterResource(image);
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



