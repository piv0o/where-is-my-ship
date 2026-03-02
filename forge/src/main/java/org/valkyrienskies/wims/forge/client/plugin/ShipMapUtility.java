package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import journeymap.client.cartography.color.RGB;
import journeymap.client.render.RenderWrapper;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.texture.Texture;
import journeymap.client.texture.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
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

    public static void drawShips(GuiGraphics graphics, Integer mouseX, Integer mouseY, double scale, Rect2i bounds) {
        PoseStack pose = graphics.pose();
        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(Minecraft.getInstance().level);
        VsiQueryableShipData<ClientShip> allShips = shipWorld.getAllShips();
        for (ShipMapPacket ship : WIMSJourneyMapPlugin.getInstance().ships) {
            ShipClientImage shipImage = WIMSJourneyMapPlugin.getInstance().images.get(ship.slug());
            if (shipImage == null) continue;
            pose.pushPose();
//            WIMSMod.LogInfo("ship %s start", ship.slug());
            var coords = new ShipClientCoordinates(allShips.getById(ship.id()), ship);

            //position to world
            pose.translate(coords.position.x(), coords.position.z(), 0);

            //draw ship image
            pose.rotateAround(coords.getQuaternion(), 0, 0, 0);
            pose.translate(-(shipImage.width() / 2f), -(shipImage.height() / 2f), 0);
            graphics.blit(shipImage.resource(), 0, 0, 0, 0, shipImage.width(), shipImage.height(), shipImage.width(), shipImage.height());

            //draw ship slug label
            pose.translate((shipImage.width() / 2f), (shipImage.height() / 2f), 0);
            pose.rotateAround(coords.getReverseQuaternion(), 0, 0, 0);
            pose.scale((float) (scale), (float) (scale), (float) (scale));
            DrawUtil.drawLabel(graphics, ship.slug(), 0, 0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.5F, 16777215, 1.0F, 1.0F, true);

            // donezo
//            WIMSMod.LogInfo("ship %s end", ship.slug());
            pose.popPose();
        }
    }

    public static void drawMiniShips(GuiGraphics graphics, Integer mouseX, Integer mouseY, double scale, Rect2i bounds, GridRenderer gridRenderer) {
        PoseStack pose = graphics.pose();
        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(Minecraft.getInstance().level);
        VsiQueryableShipData<ClientShip> allShips = shipWorld.getAllShips();
        for (ShipMapPacket ship : WIMSJourneyMapPlugin.getInstance().ships) {
            ShipClientImage shipImage = WIMSJourneyMapPlugin.getInstance().images.get(ship.slug());
            if (shipImage == null) continue;
            var coords = new ShipClientCoordinates(allShips.getById(ship.id()), ship);
            var mapCoords = gridRenderer.getPixel(coords.position.x, coords.position.z);
            if (mapCoords == null){
//                WIMSMod.LogInfo("Ship %s mapCoords is Null", ship.slug());
                continue;
            }
            pose.pushPose();
//            WIMSMod.LogInfo("X: %s %s Z: %s %s", coords.position.x, mapCoords.x, coords.position.z, mapCoords.y);
//            DrawUtil.drawColoredEntity(graphics.pose(), mapCoords.x, mapCoords.y, playerArrowBg, 16777215, 1.0F, 2.0F, (double) Math.toDegrees(coords.rotation));
//            pose.rotateAround(coords.getQuaternion(), 0, 0, 0);
            pose.translate((float) mapCoords.x + shipImage.width() / 2f, (float) mapCoords.y + shipImage.height() / 2f, 0);
            pose.scale((float) (scale), (float) (scale), (float) (scale));
            pose.mulPose(coords.getQuaternion());

            pose.translate((float) - shipImage.width() / 2f,  - shipImage.height() / 2f, 0);

//            pose.translate((float) -mapCoords.x, (float) -mapCoords.y, 0);

            graphics.blit(shipImage.resource(), 0, 0, 0, 0, shipImage.width(), shipImage.height(), shipImage.width(), shipImage.height());
//            DrawUtil.drawLabel(graphics, ship.slug(), mapCoords.x, mapCoords.y, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.5F, 16777215, 1.0F, 1.0F, true);

//            DrawUtil.drawLabel(graphics, ship.slug(), 0, 0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.5F, 16777215, 1.0F, 1.0F, true);

            pose.popPose();
        }
    }

    public static void addVertexWithUV(PoseStack poseStack, BufferBuilder buff, double x, double y, double z, double u, double v) {
        addVertexWithUV(poseStack, buff, (float) x, (float) y, (float) z, (float) u, (float) v);
    }

    public static void addVertexWithUV(PoseStack poseStack, BufferBuilder buff, float x, float y, float z, float u, float v) {
        PoseStack.Pose entry = poseStack.last();
        Matrix4f matrix4f = entry.pose();
        buff.vertex(matrix4f, x, y, z).uv(u, v).endVertex();
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

        outlineImage(img, solidBlocks);

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
            resource = new ResourceLocation("wims", "ship/" + pkt.slug());
            mc.getTextureManager().register(resource, texture);
            WIMSJourneyMapPlugin.getInstance().images.put(pkt.slug(), new ShipClientImage(resource, img.getWidth(), img.getHeight()));
        }
    }

}
