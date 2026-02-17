package org.valkyrienskies.wims.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import org.valkyrienskies.wims.ShipMapPacket;
import org.valkyrienskies.wims.WIMSMod;

public class WIMSModClient {
    public static void InitClient() {
        //

//        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WIMSMod.SHIPS_PACKET_ID, WIMSModClient::receiveShips);
    }

//    private static void receiveShips(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
//        var ships = ShipMapPacket.fromBuffer(buf);
////        ships.forEach(ship -> {
////
////        });
//    }
}
