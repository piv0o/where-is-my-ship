package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import journeymap.client.model.MapType;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.internal.ships.VsiQueryableShipData;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.client.ShipClientCoordinates;
import org.valkyrienskies.wims.client.ShipClientImage;

import java.util.ArrayList;

public class ShipMapUtility {

    public static double zLevel = 0.0D;

    public static void drawShips(GuiGraphics graphics, Integer mouseX, Integer mouseY, double scale, Rect2i bounds, MapType mapType) {
        PoseStack pose = graphics.pose();
        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(Minecraft.getInstance().level);
        VsiQueryableShipData<ClientShip> allShips = shipWorld.getAllShips();
        for (ShipMapPacket ship : WIMSJourneyMapPlugin.getInstance().ships) {
            ShipClientImage shipImage = WIMSJourneyMapPlugin.getInstance().images.get(ship.slug());
            if (shipImage == null) continue;
            pose.pushPose();
            var coords = new ShipClientCoordinates(allShips.getById(ship.id()), ship);

            //position to world
            pose.translate(coords.position.x(), coords.position.z(), 0);

            //draw ship image
            pose.rotateAround(coords.getQuaternion(), 0, 0, 0);
            pose.translate(-(shipImage.width() / 2f), -(shipImage.height() / 2f), 0);
            if(mapType.isNight()){
                RenderSystem.setShaderColor(0.2f, 0.2f, 0.2f, 1f);
            }
            if(getSettings().shipsShouldRender.get() == ShipOptions.ALWAYS || getSettings().shipsShouldRender.get() == ShipOptions.FULLSCREEN_ONLY) {
                graphics.blit(shipImage.resource(), 0, 0, 0, 0, shipImage.width(), shipImage.height(), shipImage.width(), shipImage.height());
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            //draw ship slug label
            pose.translate((shipImage.width() / 2f), (shipImage.height() / 2f), 0);
            pose.rotateAround(coords.getReverseQuaternion(), 0, 0, 0);
            pose.scale((float) (scale), (float) (scale), (float) (scale));

            if((getSettings().shipsShouldHaveLabels.get() == ShipOptions.ALWAYS || getSettings().shipsShouldHaveLabels.get() == ShipOptions.FULLSCREEN_ONLY) && ship.mass() >= Integer.parseInt(getSettings().MinMassForLabel.get().toString()) ) {
                DrawUtil.drawLabel(graphics, ship.slug(), 0, 0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.5F, 16777215, 1.0F, 1.0F, true);
            }
            // donezo
//            WIMSMod.LogInfo("ship %s end", ship.slug());
            pose.popPose();
        }
    }

    public static void drawMiniShips(GuiGraphics graphics, Integer mouseX, Integer mouseY, double scale, Rect2i bounds, GridRenderer gridRenderer, MultiBufferSource.BufferSource buffer, MapType mapType) {
        PoseStack pose = graphics.pose();
        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(Minecraft.getInstance().level);
        VsiQueryableShipData<ClientShip> allShips = shipWorld.getAllShips();
        for (ShipMapPacket ship : WIMSJourneyMapPlugin.getInstance().ships) {
            ShipClientImage shipImage = WIMSJourneyMapPlugin.getInstance().images.get(ship.slug());
            if (shipImage == null) continue;
            var coords = new ShipClientCoordinates(allShips.getById(ship.id()), ship);
            var mapCoords = gridRenderer.getPixel(coords.position.x, coords.position.z);
            if (mapCoords == null){
                continue;
            }
            pose.pushPose();


            pose.translate((float) mapCoords.x + shipImage.width() / 2f, (float) mapCoords.y + shipImage.height() / 2f, 0);
            pose.scale((float) (scale), (float) (scale), 1);
            pose.mulPose(coords.getQuaternion());
            pose.translate((float) - shipImage.width() / 2f,  - shipImage.height() / 2f, 0);

            if(mapType.isNight()){
                RenderSystem.setShaderColor(0.2f, 0.2f, 0.2f, 1f);
            }
            if(getSettings().shipsShouldRender.get() == ShipOptions.ALWAYS || getSettings().shipsShouldRender.get() == ShipOptions.MINIMAP_ONLY) {
                graphics.blit(shipImage.resource(), 0, 0, 0, 0, shipImage.width(), shipImage.height(), shipImage.width(), shipImage.height());
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            pose.translate((float)  shipImage.width() / 2f,   shipImage.height() / 2f, 10);
            pose.mulPose(coords.getReverseQuaternion());
            pose.scale((float) (1/scale), (float) (1/scale), 1);
            if((getSettings().shipsShouldHaveLabels.get() == ShipOptions.ALWAYS || getSettings().shipsShouldHaveLabels.get() == ShipOptions.MINIMAP_ONLY) && ship.mass() >= getSettings().MinMassForLabel.get()){
                DrawUtil.drawBatchLabel(graphics.pose(), Component.literal(ship.slug()), buffer, 0, 0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.5F, 16777215, 1.0F, 1.0F, true);
            }

            pose.popPose();
        }
    }


    private static WIMSForgeClientProperties getSettings(){
        return WIMSJourneyMapPlugin.getInstance().getClientProperties();
    }


    private static NativeImage convertBytes(ShipImagePacket pkt) {
        byte[] data = pkt.data();

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, pkt.width() + 2, pkt.height() + 2, true);

        boolean[][] solidBlocks = new boolean[pkt.width() + 2][pkt.height() + 2];

        int i = 0;
        for (int y = 0; y < pkt.height(); y++) {
            for (int x = 0; x < pkt.width(); x++) {
                int r = data[i++] & 0xFF;
                int g = data[i++] & 0xFF;
                int b = data[i++] & 0xFF;
                int a = data[i++] & 0xFF;
                int rgba = (a << 24) | (b << 16) | (g << 8) | r; // fuck this value
                solidBlocks[x + 1][y + 1] = a != 0;
                img.setPixelRGBA(x + 1, y + 1, rgba);

            }
        }

        if(WIMSJourneyMapPlugin.getInstance().getClientProperties().shipsHaveOutline.get()){
            outlineImage(img, solidBlocks);
        }

        return img;
    }

    private static void outlineImage(NativeImage img, boolean[][] solidBlocks) {
        int width = img.getWidth();
        int height = img.getHeight();
        ArrayList<Integer[]> outLines = new ArrayList<Integer[]>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isPixelTransparent(solidBlocks, x, y) && isNeighborOpaque(solidBlocks, x, y)) {
                    outLines.add(new Integer[]{x, y});
                }
            }
        }

        for (Integer[] outLine : outLines) {
            img.setPixelRGBA(outLine[0], outLine[1], getOutlineColour());
        }
    }

    public static int getOutlineColour() {
        return 0xFF000000;
    }

    private static boolean isPixelTransparent(boolean[][] solidBlocks, int x, int y) {
        if (x < 0 || y < 0 || x >= solidBlocks.length || y >= solidBlocks[x].length) return true;
        return !solidBlocks[x][y];
    }

    private static boolean isNeighborOpaque(boolean[][] solidBlocks, int x, int y) {
        return !isPixelTransparent(solidBlocks, x + 1, y)
                || !isPixelTransparent(solidBlocks, x - 1, y)
                || !isPixelTransparent(solidBlocks, x, y + 1)
                || !isPixelTransparent(solidBlocks, x, y - 1);
    }


//doesn't work unless we have transparency, maybe attempt it but it's not a high priority
//    private static boolean isNeighborOpaque2(boolean[][] solidBlocks, int x, int y) {
//        return !isPixelTransparent(solidBlocks, x - 1, y)
//                || !isPixelTransparent(solidBlocks, x, y - 1);
//    }


    public static void RegisterResource(ShipImagePacket pkt) {
        NativeImage img = convertBytes(pkt);
        var mc = Minecraft.getInstance();

        DynamicTexture texture;
        ResourceLocation resource;
        if (WIMSJourneyMapPlugin.getInstance().images.containsKey(pkt.slug())) {
            resource = WIMSJourneyMapPlugin.getInstance().images.get(pkt.slug()).resource();
            texture = (DynamicTexture) mc.getTextureManager().getTexture(resource);
            mc.execute(() -> {
                mc.getTextureManager().register(resource, new DynamicTexture(img));
                texture.close();
                WIMSJourneyMapPlugin.getInstance().images.put(pkt.slug(), new ShipClientImage(resource, img.getWidth(), img.getHeight()));
            });
        } else {
            texture = new DynamicTexture(img);
            //noinspection removal
            resource = new ResourceLocation("wims", "ship/" + pkt.slug());
            mc.getTextureManager().register(resource, texture);
            WIMSJourneyMapPlugin.getInstance().images.put(pkt.slug(), new ShipClientImage(resource, img.getWidth(), img.getHeight()));
        }
    }

}
