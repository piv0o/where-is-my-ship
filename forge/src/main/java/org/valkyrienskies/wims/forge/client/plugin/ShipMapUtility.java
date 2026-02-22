package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;

public class ShipMapUtility {

    public static void drawShips(GuiGraphics graphics, int mouseX, int mouseY, float pt, double scale, Rect2i bounds) {
        PoseStack pose = graphics.pose();
        for (ShipMapPacket ship : WIMSJourneyMapPlugin.getInstance().ships) {

//            WIMSMod.LogInfo("ship %s x:%s z:%s r:%s", ship.slug(), ship.worldPos().x(), ship.worldPos().z(), ship.getTrueRotation());

            var res = WIMSJourneyMapPlugin.getInstance().images.get(ship.slug());
            if (res == null) continue;
            pose.pushPose();

            pose.translate(ship.worldPos().x(), ship.worldPos().z(), 0);
            pose.rotateAround(Axis.ZP.rotation((float) Math.toRadians(ship.getTrueRotation())), 0, 0, 0);
            pose.translate(-(ship.getWidth() / 2f), -(ship.getHeight() / 2f), 0);

            graphics.blit(res, 0, 0, 0, 0, ship.getWidth(), ship.getHeight(), ship.getWidth(), ship.getHeight());

            pose.translate((ship.getWidth() / 2f), (ship.getHeight() / 2f), 0);
            pose.rotateAround(Axis.ZP.rotation((float) -Math.toRadians(ship.getTrueRotation())), 0, 0, 0);
            pose.scale((float) (scale), (float) (scale), (float) (scale));

            var font = Minecraft.getInstance().font;
            graphics.fill(font.width(ship.slug())/2,10, -font.width(ship.slug())/2, -10,0X000000FF);
            graphics.drawCenteredString(font, ship.slug(), 0,0, 0xffffff);
            pose.popPose();
        }
    }


    private static NativeImage convertBytes(ShipImagePacket pkt) {
        byte[] data = pkt.data();

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, pkt.width(), pkt.height(), false);

        int i = 0;
        for (int y = 0; y < pkt.height(); y++) {
            for (int x = 0; x < pkt.width(); x++) {
                int r = data[i++] & 0xFF;
                int g = data[i++] & 0xFF;
                int b = data[i++] & 0xFF;
                int a = data[i++] & 0xFF;
                int rgba = (a << 24) | (b << 16) | (g << 8) | r; // fuck this value
                img.setPixelRGBA(x, y, rgba);

            }
        }

        return img;
    }

    public static void RegisterResource(ShipImagePacket pkt) {
        NativeImage img = convertBytes(pkt);
        var mc = Minecraft.getInstance();

        DynamicTexture texture;
        ResourceLocation resource;
        if (WIMSJourneyMapPlugin.getInstance().images.containsKey(pkt.slug())) {
            resource = WIMSJourneyMapPlugin.getInstance().images.get(pkt.slug());
            texture = (DynamicTexture) mc.getTextureManager().getTexture(resource);
            mc.execute(()->{
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
