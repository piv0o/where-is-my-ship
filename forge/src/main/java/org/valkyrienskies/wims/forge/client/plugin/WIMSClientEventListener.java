package org.valkyrienskies.wims.forge.client.plugin;
import journeymap.client.api.IClientAPI;
import org.valkyrienskies.wims.ShipMapPacket;

import java.util.ArrayList;

public class WIMSClientEventListener {

    private static IClientAPI jmAPI;

    private static ArrayList<ShipMapPacket> ships;

    WIMSClientEventListener(IClientAPI jmAPI){
        WIMSClientEventListener.jmAPI = jmAPI;


    }
}
