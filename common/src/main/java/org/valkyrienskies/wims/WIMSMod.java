package org.valkyrienskies.wims;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.internal.ships.VsiQueryableShipData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;


public class WIMSMod {
    public static final String MOD_ID = "wims";

    public static final ResourceLocation SHIPS_PACKET_ID = new ResourceLocation(WIMSMod.MOD_ID, "ships_packet_id");
    public static final ResourceLocation SHIPS_IMAGE_PACKET_ID = new ResourceLocation(WIMSMod.MOD_ID, "ships_image_packet_id");

    public static int timer = 0;

    public static void Init() {
        //

        TickEvent.SERVER_LEVEL_POST.register(WIMSMod::tick);
    }


    private static void tick(ServerLevel server) {
        ServerLevel level = server.getLevel();
        VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        VsiQueryableShipData<ServerShip> ships = shipWorld.getAllShips();
        ArrayList<ShipMapPacket> packets = new ArrayList<ShipMapPacket>();
        ShipImagePacket image = null;
        int shipCount = ships.toArray().length;
        int i = 0;
        for (ServerShip ship : ships) {
            var AABB = ship.getShipAABB();
            if (timer % (shipCount * 60) == (i * 60) && AABB != null) {
                byte[] data = ShipRasterizer.generateImageData(ship, level);
                if (data != null) {
                    image = new ShipImagePacket(
                            ship.getSlug(),
                            AABB.maxX() - AABB.minX(),
                            AABB.maxZ() - AABB.minZ(),
                            data.length,
                            data
                    );
                }
            }
            i++;
        }
        ships.forEach(ship -> {
            var packet = ShipMapPacket.fromShip(ship, level);
            if (packet != null) packets.add(packet);
        });
        ShipImagePacket finalImage = image;
        server.players().forEach(player -> {
            NetworkManager.sendToPlayer(player, SHIPS_PACKET_ID, ShipMapPacket.toBuffer(packets));
            if (finalImage != null)
                NetworkManager.sendToPlayer(player, SHIPS_IMAGE_PACKET_ID, ShipImagePacket.toBuffer(finalImage));
        });
        timer++;
    }
}
