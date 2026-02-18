package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WIMSImageOverlay {


    public static void updateImage(ShipMapPacket ship, IClientAPI jmAPI) {
        if (WIMSJourneyMapPlugin.getInstance().shipOverlays.containsKey(ship.slug())) {
            var overlay = WIMSJourneyMapPlugin.getInstance().shipOverlays.get(ship.slug());
            overlay.setNorthWestPoint(ship.worldPos().subtract(new BlockPos(1, 1, 1)));
            overlay.setSouthEastPoint(ship.getWorldPos2().subtract(new BlockPos(1, 1, 1)));
            setRotation(overlay.getImage(), ship);
        } else {
            WIMSJourneyMapPlugin.getInstance().shipOverlays.put(ship.slug(), createOverlay(ship, jmAPI));
        }
    }

    public static ImageOverlay createOverlay(ShipMapPacket ship, IClientAPI jmAPI) {
        try {
            MapImage image = getShipImage(ship.slug(), ship.getWidth(), ship.getHeight());


            String displayID = ship.slug(); //String.format("image%s,%s,%s,%s", ship.worldPos().getX(), ship.worldPos().getZ(), ship.getWidth(), ship.getHeight());
            ImageOverlay overlay = new ImageOverlay(WIMSMod.MOD_ID, displayID, ship.worldPos().subtract(new BlockPos(1, 1, 1)), ship.getWorldPos2().subtract(new BlockPos(1, 1, 1)), image);

            overlay.setDimension(Minecraft.getInstance().player.level().dimension());
            overlay.setLabel(ship.slug())
                    .setTitle(ship.slug());
            jmAPI.show(overlay);
//            WIMSMod.LogInfo(String.format("rotation 1: %s %s %s", ship.rotY(), (int) (Math.toDegrees(ship.rotY())), (int) (Math.toDegrees(ship.rotY()) + 180)));
            WIMSMod.LogInfo(String.format("rotation for %s: %s, x:%s, y:%s, z:%s,", ship.slug(), ship.getTrueRotation(), (int) Math.toDegrees(ship.rotX()), (int) Math.toDegrees(ship.rotY()), (int) Math.toDegrees(ship.rotZ()) ));
            setRotation(image, ship);
            return overlay;
        } catch (Throwable t) {
            WIMSMod.LogError(t.getMessage());
            return null;
        }
    }

    public static void setRotation(MapImage image, ShipMapPacket ship) {
        image.setRotation(ship.getTrueRotation());
    }

    public static NativeImage convertBytes(ShipImagePacket pkt) {
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
//                WIMSMod.LogInfo(String.format("pixel %s %s: %s %s %s %s", x, y, r, g, b, a));
                img.setPixelRGBA(x, y, rgba);

            }
        }

        return img;
    }

    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();

    //TODO: CLEAN Entry
    private record Entry(ResourceLocation resource, DynamicTexture texture, MapImage mapImage, int width, int height) {
    }

    public static ResourceLocation RegisterResource(ShipImagePacket pkt, NativeImage img) {
        var mc = Minecraft.getInstance();
        ResourceLocation resource = new ResourceLocation("wims", "ship/" + pkt.slug());
        Entry old = CACHE.remove(pkt.slug());
        if (old != null) {
            mc.getTextureManager().release(old.resource());
            old.texture().close(); // safe to close after release
        }
        DynamicTexture texture = new DynamicTexture(img);
        mc.getTextureManager().register(resource, texture);
        var tex = mc.getTextureManager().getTexture(resource);
        CACHE.put(pkt.slug(), new Entry(resource, texture, null, pkt.width(), pkt.height()));
        return resource;
    }

    public static MapImage getShipImage(String slug, int width, int height) {
        if (WIMSJourneyMapPlugin.getInstance().images.containsKey(slug)) {
            return new MapImage(WIMSJourneyMapPlugin.getInstance().images.get(slug), width, height);
        } else {
            return new MapImage(createNativeImage(width, height));
        }
    }

    public static NativeImage createNativeImage(int width, int height) {
        return new NativeImage(NativeImage.Format.LUMINANCE, width, height, false);
    }
}
