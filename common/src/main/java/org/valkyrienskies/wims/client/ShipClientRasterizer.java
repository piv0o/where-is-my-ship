package org.valkyrienskies.wims.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.wims.WIMSMod;

public class ShipClientRasterizer {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public static byte[] generateImageData(ClientShip ship, ClientLevel level) {
        var shipAABB = ship.getShipAABB();
        if (shipAABB == null) return null;
        int minX = shipAABB.minX();
        int minY = shipAABB.minY();
        int minZ = shipAABB.minZ();
        int maxX = shipAABB.maxX();
        int maxY = shipAABB.maxY();
        int maxZ = shipAABB.maxZ();

        int width = maxX - minX + 1;
        int height = maxZ - minZ + 1;
        byte[] data = new byte[width * height * 4];

        int r;
        int g;
        int b;
        int a;
        int rgb;
        int i = 0;
        int index;
        float shadowMult;
        for (int z = minZ; z < maxZ; z++) {
            for (int x = minX; x < maxX; x++) {
                r = 0x00;
                g = 0x00;
                b = 0x00;
                a = 0x00;
                for (int y = maxY; y >= minY; y--) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (isSolidBlock(level, state)) {
                        MapColor mc = state.getMapColor(level, pos);
                        rgb = mc.col;
                        shadowMult = 1;
                        if (isInShadow(level, x, y, z)) {
                            shadowMult = 0.75f;
                        }
                        r = (int) (((rgb >> 16) & 0xFF) * shadowMult);
                        g = (int) (((rgb >> 8) & 0xFF) * shadowMult);
                        b = (int) ((rgb & 0xff) * shadowMult);
                        a = 0xFF;

                        break;
                    }
                }
                index = i * 4;
                data[index] = (byte) r;
                data[index + 1] = (byte) g;
                data[index + 2] = (byte) b;
                data[index + 3] = (byte) a;
                i++;
            }
        }
        return data;
    }

    public static boolean isSolidBlock(ClientLevel level, int x, int y, int z) {
        pos.set(x, y, z);
        BlockState state = level.getBlockState(pos);
        return isSolidBlock(level, state);
    }

    public static boolean isSolidBlock(ClientLevel level, BlockState state) {
        return !state.isAir()
                && !state.getFluidState().isSource()
                && state.isSolidRender(level, pos);
    }

    public static boolean isInShadow(ClientLevel level, int x, int y, int z) {
        return isSolidBlock(level, x - 1, y + 1, z - 1)
                || isSolidBlock(level, x - 1, y + 1, z)
                || isSolidBlock(level, x, y + 1, z - 1);
    }
}
