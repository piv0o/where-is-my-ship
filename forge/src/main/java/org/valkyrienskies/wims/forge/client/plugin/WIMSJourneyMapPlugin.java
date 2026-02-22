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
import java.util.EnumSet;
import java.util.HashMap;

import journeymap.client.api.util.UIState;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;

@journeymap.client.api.ClientPlugin
public class WIMSJourneyMapPlugin implements IClientPlugin {
    private IClientAPI jmAPI = null;

    // Forge listener reference
    private static WIMSJourneyMapPlugin INSTANCE;

    public ArrayList<ShipMapPacket> ships;
    public HashMap<String, ResourceLocation> images = new HashMap<>();

    private boolean isMappingStarted;

    public WIMSJourneyMapPlugin() {
        INSTANCE = this;
    }

    public static WIMSJourneyMapPlugin getInstance() {
        return INSTANCE;
    }

    // grabbing from JourneyMapFullscreenMixin
    // heavily based on create train map renderer
    public static void OnRender(GuiGraphics graphics, Fullscreen screen, double x, double z, int mX, int mY, float pt, FullMapProperties fullMapProperties) {
        UIState state = screen.getUiState();
        if (state == null) return;
        if (state.ui != Context.UI.Fullscreen) return;
        if (!state.active) return;

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        double guiScale = (double) window.getScreenWidth() / window.getGuiScaledWidth();
        double scale = state.blockSize / guiScale;

        PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(screen.width / 2.0f, screen.height / 2.0f, 0);
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

        ShipMapUtility.drawShips(graphics, (int) Math.floor(mouseX), (int) Math.floor(mouseY), pt, fullMapProperties.fontScale.get()/scale/4, bounds);
        pose.popPose();
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



