package org.valkyrienskies.wims.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import org.valkyrienskies.wims.client.WIMSModClient;

public class WIMSModFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WIMSModClient.InitClient();
    }
}
