package org.valkyrienskies.wims;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.ServerShip;
import java.util.ArrayList;

public record ShipMapPacket(
        long id,
        String slug,
        String dim,
        Vector3f worldPos,
        Vector3f worldVel,
        BlockPos shipPos1,
        BlockPos shipPos2,
        double rotX,
        double rotY,
        double rotZ,
        double AVX,
        double AVY,
        double AVZ,
        double mass

) {
    public static FriendlyByteBuf toBuffer(Iterable<ShipMapPacket> markers) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        for (ShipMapPacket marker : markers) {
            buf.writeLong(marker.id);
            buf.writeUtf(marker.slug);
            buf.writeUtf(marker.dim);
            buf.writeVector3f(marker.worldPos);
            buf.writeVector3f(marker.worldVel);
            buf.writeBlockPos(marker.shipPos1);
            buf.writeBlockPos(marker.shipPos2);
            buf.writeDouble(marker.rotX);
            buf.writeDouble(marker.rotY);
            buf.writeDouble(marker.rotZ);
            buf.writeDouble(marker.AVX);
            buf.writeDouble(marker.AVY);
            buf.writeDouble(marker.AVZ);
            buf.writeDouble(marker.mass);
        }
        return buf;
    }

    public static ShipMapPacket fromShip(ServerShip ship, ServerLevel level) {
        var shipAABB = ship.getShipAABB();
        var rotation = new Vector3d();
        var center = new Vector3f();
         ship.getWorldAABB().center(center);
        var velocity = ship.getVelocity();
        var angularVelocity = ship.getKinematics().getAngularVelocity();
        ship.getKinematics().getRotation().getEulerAnglesXYZ(rotation);
        if (shipAABB != null) {
            return new ShipMapPacket(
                    ship.getId(),
                    ship.getSlug(),
                    level.toString(),
                    center,
                    new Vector3f((float) velocity.x(), (float) velocity.y(), (float) velocity.z()),
                    new BlockPos(shipAABB.minX(), shipAABB.minY(), shipAABB.minZ()),
                    new BlockPos(shipAABB.maxX(), shipAABB.maxY(), shipAABB.maxZ()),
                    rotation.x,
                    rotation.y,
                    rotation.z,
                    angularVelocity.x(),
                    angularVelocity.y(),
                    angularVelocity.z(),
                    ship.getInertiaData().getMass()
            );
        } else {
            return null;
        }
    }

    public int getTrueRotation() {
        return getTrueRotation(rotX, rotY, rotZ);
    }

    public static int getTrueRotation(double rotX, double rotY, double rotZ) {
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

    public static ArrayList<ShipMapPacket> fromBuffer(FriendlyByteBuf buf) {
        ArrayList<ShipMapPacket> out = new ArrayList<>();
        while (buf.readableBytes() > 0) {
            out.add(new ShipMapPacket(
                    buf.readLong(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVector3f(),
                    buf.readVector3f(),
                    buf.readBlockPos(),
                    buf.readBlockPos(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            ));
        }
        return out;
    }
}
