package org.valkyrienskies.wims;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public record ShipMapPacket(
        String slug,
        String dim,
        Vector3f worldPos,
        BlockPos shipPos1,
        BlockPos shipPos2,
        double rotX,
        double rotY,
        double rotZ


) {
    public static FriendlyByteBuf toBuffer(Iterable<ShipMapPacket> markers) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        for (ShipMapPacket marker : markers) {
            buf.writeUtf(marker.slug);
            buf.writeUtf(marker.dim);
            buf.writeVector3f(marker.worldPos);
            buf.writeBlockPos(marker.shipPos1);
            buf.writeBlockPos(marker.shipPos2);
            buf.writeDouble(marker.rotX);
            buf.writeDouble(marker.rotY);
            buf.writeDouble(marker.rotZ);
        }
        return buf;
    }

    public static ShipMapPacket fromShip(ServerShip ship, ServerLevel level) {
        var shipAABB = ship.getShipAABB();
        var rotation = new Vector3d();
        var position = ship.getKinematics().getPosition();
        ship.getKinematics().getRotation().getEulerAnglesXYZ(rotation);
        byte[] img = null;
        if (shipAABB != null) {
            return new ShipMapPacket(
                    ship.getSlug(),
                    level.toString(),
                    new Vector3f((float) position.x(), (float) position.y(), (float) position.z()),
                    new BlockPos(shipAABB.minX(), shipAABB.minY(), shipAABB.minZ()),
                    new BlockPos(shipAABB.maxX(), shipAABB.maxY(), shipAABB.maxZ()),
                    rotation.x,
                    rotation.y,
                    rotation.z
            );
        } else {
            return null;
        }
    }


    private static BlockPos VectorToBlockPos(Vector3dc vec) {
        return new BlockPos((int) Math.round(vec.x()), (int) Math.round(vec.y()), (int) Math.round((vec.z())));
    }

    public int getWidth() {
        return shipPos2.getX() - shipPos1.getX();
    }

    public int getHeight() {
        return shipPos2.getZ() - shipPos1.getZ();
    }

//    public BlockPos getWorldPos2() {
//        return worldPos.offset(shipPos2.subtract(shipPos1));
//    }

    public BlockPos GetDimensions() {
        return shipPos2.subtract(shipPos1);
    }

    public BlockPos GetHalfDimsions() {
        var dimensions = GetDimensions();
        return new BlockPos(
                Math.round((float) dimensions.getX() / 2),
                Math.round((float) dimensions.getY() / 2),
                Math.round((float) dimensions.getZ() / 2)
        );
    }

    public int getTrueRotation() {
        var cx = Math.cos(rotX);
        var cy = Math.cos(rotY);
        var cz = Math.cos(rotZ);

        var sx = Math.sin(rotX);
        var sy = Math.sin(rotY);
        var sz = Math.sin(rotZ);

        var fx = cy * cz;
        var fz = sx * sz - cx * sy * cz;
        var ang = (Math.atan2(fz, fx) * 180 / Math.PI + 360) % 360;
        return (int) Math.round(ang);
    }

//    public BlockPos getWorldPos1_dep() {
//        return worldPos.subtract(GetHalfDimsions());
//    }
//
//    public BlockPos getWorldPos2_dep() {
//        return worldPos.offset(GetHalfDimsions());
//    }

    public static ArrayList<ShipMapPacket> fromBuffer(FriendlyByteBuf buf) {
        ArrayList<ShipMapPacket> out = new ArrayList<ShipMapPacket>();
        while (buf.readableBytes() > 0) {
            out.add(new ShipMapPacket(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVector3f(),
                    buf.readBlockPos(),
                    buf.readBlockPos(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            ));
        }
        return out;
    }
}
