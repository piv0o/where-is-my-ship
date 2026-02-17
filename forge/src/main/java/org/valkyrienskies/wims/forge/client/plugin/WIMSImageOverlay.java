package org.valkyrienskies.wims.forge.client.plugin;

import com.mojang.blaze3d.platform.NativeImage;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.valkyrienskies.wims.ShipImagePacket;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.forge.WIMSModForge;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.InflaterInputStream;



import static org.apache.commons.compress.utils.ArchiveUtils.sanitize;


public class WIMSImageOverlay {
    public static void CreateImage(ShipMapPacket ship, IClientAPI jmAPI) {
        createOverlays(ship, jmAPI, 1);
    }

    public static void removeOverlays(IClientAPI jmAPI, ArrayList<ImageOverlay> overlays) {
        for (ImageOverlay overlay : overlays) {
            jmAPI.remove(overlay);
        }
    }

    public static ArrayList<ImageOverlay> createOverlays(ShipMapPacket ship, IClientAPI jmAPI, int quality) {
        ArrayList<ImageOverlay> out = new ArrayList<ImageOverlay>();
        for (int i = 0; i < quality; i++) {
            out.add(createOverlay(ship, jmAPI));
        }
        return out;
    }

    public static ImageOverlay createOverlay(ShipMapPacket ship, IClientAPI jmAPI) {
        try {
            WIMSModForge.LogInfo(String.format("%s : %s %s | %s %s %s", ship.slug(), ship.getWidth(), ship.getHeight(), Math.toDegrees(ship.rotX()), Math.toDegrees(ship.rotY()), Math.toDegrees(ship.rotZ())));
            MapImage image = getShipImage( ship.slug(),ship.getWidth(), ship.getHeight());
//            image.centerAnchors();
            String displayID =  ship.slug(); //String.format("image%s,%s,%s,%s", ship.worldPos().getX(), ship.worldPos().getZ(), ship.getWidth(), ship.getHeight());
            ImageOverlay overlay = new ImageOverlay(WIMSMod.MOD_ID, displayID, ship.worldPos(), ship.getWorldPos2(), image);

            overlay.setDimension(Minecraft.getInstance().player.level().dimension());
            overlay.setLabel(ship.slug())
                    .setTitle(ship.slug());
            jmAPI.show(overlay);
            image.setRotation((int) (Math.toDegrees(ship.rotY())));
            return overlay;
        } catch (Throwable t) {
            WIMSModForge.LogError(t.getMessage());
            return null;
        }
    }

//    public static removeImage(String slug, IClientAPI jmAPI){
//
//    }
//    public static NativeImage convertBytes(ShipImagePacket pkt) {
//        NativeImage img = null;
//        try {
//            img = NativeImage.read(pkt.data());
//        } catch (IOException e) {
//            img = createNativeImage(pkt.width(), pkt.height());
//        }
//
//        return img;
//    }
    public static NativeImage convertBytes(ShipImagePacket pkt) {
        byte[] data = inflate(pkt.data(), pkt.dataLength());

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, pkt.width(), pkt.height(), false);

        int i = 0;
        for (int y = 0; y < pkt.height(); y++) {
            for (int x = 0; x < pkt.width(); x++) {
                int r = data[i++] & 0xFF;
                int g = data[i++] & 0xFF;
                int b = data[i++] & 0xFF;
                int a = data[i++] & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r; // NativeImage uses ABGR in setPixelRGBA

                img.setPixelRGBA(x, y, abgr);
            }
        }

        return img;
    }

    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
    //TODO: CLEAN Entry
    private record Entry(ResourceLocation resource, DynamicTexture texture, MapImage mapImage, int width, int height) {}

    public static ResourceLocation RegisterResource(ShipImagePacket pkt ,NativeImage img){
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
//        tex.setFilter(false, true);     // blur=false, mipmap=false  (common)
//        tex.setBlurMipmap(false, true); // older name in some mappings
        CACHE.put(pkt.slug(), new Entry(resource, texture, null, pkt.width(), pkt.height()));
        return resource;
    }

    private static byte[] inflate(byte[] z, int expectedLen) {
        try {
            byte[] out = new byte[expectedLen];
            try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(z))) {
                int off = 0;
                while (off < expectedLen) {
                    int r = in.read(out, off, expectedLen - off);
                    if (r < 0) break;
                    off += r;
                }
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MapImage getShipImage(String slug, int width, int height){
        if(WIMSJourneyMapPlugin.getInstance().images.containsKey(slug)){
//            WIMSModForge.LogInfo(String.format("using slug %s", slug));
//            WIMSModForge.LogInfo(String.format("using resource  %s", WIMSJourneyMapPlugin.getInstance().images.get(slug).toString()));
            return new MapImage(WIMSJourneyMapPlugin.getInstance().images.get(slug), width, height);
        } else {
            return new MapImage(createNativeImage(width, height));
        }
    }

    public static NativeImage createNativeImage(int width, int height) {

        return new NativeImage(NativeImage.Format.LUMINANCE, width, height, false);
    }
}
