package org.valkyrienskies.wims.client;

import com.mojang.math.Axis;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.wims.ShipMapPacket;

public class ShipClientCoordinates {
    public Vector3f position;
    public float rotation;
    public double mass;
    public ShipClientCoordinates(ClientShip ship, ShipMapPacket pkt){
        if(ship != null){
            Vector3d r = new Vector3d();
            position = new Vector3f();

            ship.getWorldAABB().center(position);
            ship.getKinematics().getRotation().getEulerAnglesXYZ(r);

            rotation = (float) Math.toRadians(ShipMapPacket.getTrueRotation(r.x, r.y, r.z));
        } else {
            position = pkt.worldPos();
            rotation = (float) Math.toRadians(pkt.getTrueRotation());
        }
        mass = pkt.mass();
    }

    public Quaternionf getQuaternion(){
        return Axis.ZP.rotation(rotation);
    }

    public Quaternionf getReverseQuaternion(){
        return Axis.ZP.rotation(-rotation);
    }
}
