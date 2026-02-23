package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import journeymap.client.render.draw.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;

import java.util.ArrayList;

public class ShipMapUtility {

    public static void drawShips(GuiGraphics graphics, int mouseX, int mouseY, double scale, Rect2i bounds) {
        PoseStack pose = graphics.pose();
        for (ShipMapPacket ship : WIMSJourneyMapPlugin.getInstance().ships) {

//            WIMSMod.LogInfo("ship %s x:%s z:%s r:%s", ship.slug(), ship.worldPos().x(), ship.worldPos().z(), ship.getTrueRotation());

            var res = WIMSJourneyMapPlugin.getInstance().images.get(ship.slug());
            if (res == null) continue;
            pose.pushPose();

            pose.translate(ship.worldPos().x(), ship.worldPos().z(), 0);
            pose.rotateAround(Axis.ZP.rotation((float) Math.toRadians(ship.getTrueRotation())), 0, 0, 0);
            pose.translate(-(ship.getWidth() / 2f), -(ship.getHeight() / 2f), 0);

            graphics.blit(res, -1, -1, 0, 0, ship.getWidth() + 2, ship.getHeight() + 2, ship.getWidth() + 2, ship.getHeight() + 2);

            pose.translate((ship.getWidth() / 2f), (ship.getHeight() / 2f), 0);
            pose.rotateAround(Axis.ZP.rotation((float) -Math.toRadians(ship.getTrueRotation())), 0, 0, 0);
            pose.scale((float) (scale), (float) (scale), (float) (scale));

            DrawUtil.drawLabel(graphics, ship.slug(), 0, /*centerZ += (double)20.0F*/0, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, 0, 0.5F, 16777215, 1.0F, (double)1.0F, true);
//            graphics.fill(font.width(ship.slug()) / 2, 10, -font.width(ship.slug()) / 2, -10, 0X000000FF);
//            graphics.drawCenteredString(font, ship.slug(), 0, 0, 0xffffff);
            pose.popPose();
        }
    }


    private static NativeImage convertBytes(ShipImagePacket pkt) {
        byte[] data = pkt.data();

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, pkt.width() + 2, pkt.height() + 2, false);

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

    public static int getOutlineColour(){
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
            resource = WIMSJourneyMapPlugin.getInstance().images.get(pkt.slug());
            texture = (DynamicTexture) mc.getTextureManager().getTexture(resource);
            mc.execute(() -> {
                mc.getTextureManager().register(resource, new DynamicTexture(img));
                texture.close();
            });
        } else {
            texture = new DynamicTexture(img);
            resource = new ResourceLocation("wims", "ship/" + pkt.slug());
            mc.getTextureManager().register(resource, texture);
            WIMSJourneyMapPlugin.getInstance().images.put(pkt.slug(), resource);
        }
    }

}
