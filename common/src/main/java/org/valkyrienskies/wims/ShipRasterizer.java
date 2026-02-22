package org.valkyrienskies.wims;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;

public class ShipRasterizer {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public static byte[] generateImageData(ServerShip ship, ServerLevel level) {
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
                    pos.set(x,y,z);
                    BlockState state = level.getBlockState(pos);
                    if (IsSolidBlock(level, state)) {
                        MapColor mc = state.getMapColor(level, pos);
                        rgb = mc.col;
                        shadowMult = 1;
                        if (isInShadow(level, x, y, z)) {
                            shadowMult = 0.75f;
                        }
                        r = (int) (((rgb >> 16) & 0xFF) * shadowMult);
                        g = (int) (((rgb >> 8) & 0xFF) * shadowMult);
                        b = (int) ((rgb & 0xff) * shadowMult);
                        a = 0xff;
                        break;
                    }
                }
                index = i * 4;
                WIMSMod.LogInfo("i: %s rgba %s %s %s %s", i, r, g, b, a);
                data[index] = (byte) r;
                data[index + 1] = (byte) g;
                data[index + 2] = (byte) b;
                data[index + 3] = (byte) a;
                i++;
            }
        }
        return data;
    }

    private static boolean IsSolidBlock(ServerLevel level, int x, int y, int z) {
        pos.set(x, y, z);
        BlockState state = level.getBlockState(pos);
        return IsSolidBlock(level, state);
    }

    private static boolean IsSolidBlock(ServerLevel level, BlockState state) {
        return !state.isAir() && !state.getFluidState().isSource() && state.isSolidRender(level, pos);
    }

    private static boolean isInShadow(ServerLevel level, int x, int y, int z) {
        return IsSolidBlock(level, x - 1, y + 1, z - 1)
                || IsSolidBlock(level, x - 1, y + 1, z)
                || IsSolidBlock(level, x, y + 1, z - 1);
    }
}
