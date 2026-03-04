package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Context;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.IClientAPI;

import java.util.*;

import journeymap.client.api.event.RegistryEvent;
import journeymap.client.api.util.UIState;
import journeymap.client.model.MapState;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.client.ShipClientImage;

import static journeymap.client.api.event.ClientEvent.Type.*;


@journeymap.client.api.ClientPlugin
public class WIMSJourneyMapPlugin implements IClientPlugin {

    // Forge listener reference
    private static WIMSJourneyMapPlugin INSTANCE;

    public ArrayList<ShipMapPacket> ships;
    public HashMap<String, ShipClientImage> images = new HashMap<>();

    public WIMSJourneyMapPlugin() {
        INSTANCE = this;
    }

    public static WIMSJourneyMapPlugin getInstance() {
        return INSTANCE;
    }

    private WIMSForgeClientProperties clientProperties;

    public static void onFullscreenRender(GuiGraphics graphics, Fullscreen screen, double x, double z, int mX, int mY, FullMapProperties fullMapProperties, MapState mapState) {
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

        ShipMapUtility.drawShips(graphics, (int) Math.floor(mouseX), (int) Math.floor(mouseY), 1f / scale, bounds, mapState.getMapType());
        pose.popPose();
    }

    public static void onMinimapRender(GuiGraphics graphics, MiniMap screen, double x, double z, GridRenderer gridRenderer, MiniMapProperties miniMapProperties, MapState mapState) {
        try {
            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource.BufferSource buffer = graphics.bufferSource();
            var scale = Math.pow(2.0D, (double) miniMapProperties.zoomLevel.get());
            if (mc.player != null) {
                ShipMapUtility.drawMiniShips(graphics, null, null, scale, null, gridRenderer, buffer, mapState.getMapType());
            }
        } catch (Exception e) {
            WIMSMod.logError(e.getMessage());
            WIMSMod.logError(Arrays.toString(e.getStackTrace()));
        }

    }

    public WIMSForgeClientProperties getClientProperties() {
        return clientProperties;
    }

    @Override
    public void initialize(final IClientAPI jmAPI) {

        jmAPI.subscribe(getModId(), EnumSet.of(REGISTRY));

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_PACKET_ID, WIMSJourneyMapPlugin::receiveShips);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_IMAGE_PACKET_ID, WIMSJourneyMapPlugin::receiveImage);
    }

    private static void receiveShips(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        WIMSJourneyMapPlugin.INSTANCE.ships = ShipMapPacket.fromBuffer(buf);
    }

    private static void receiveImage(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ShipImagePacket image = ShipImagePacket.fromBuffer(buf);
        ShipMapUtility.registerResource(image);
    }

    @Override
    public String getModId() {
        return WIMSMod.MOD_ID;
    }

    @Override
    public void onEvent(@NotNull ClientEvent event) {
        try {
            if (event.type == ClientEvent.Type.REGISTRY) {
                RegistryEvent registryEvent = (RegistryEvent) event;

                if (registryEvent.getRegistryType() == RegistryEvent.RegistryType.OPTIONS) {
                    this.clientProperties = new WIMSForgeClientProperties();
                }
            }
        } catch (Throwable t) {
            WIMSMod.logError(t.getMessage());
        }
    }

}



