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

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int r;
        int g;
        int b;
        int a;
        int i = 0;
        for (int z = minZ; z < maxZ; z++) {
            for (int x = minX; x < maxX; x++) {
                r = 0x00;
                g = 0x00;
                b = 0x00;
                a = 0x00;
                for (int y = maxY; y >= minY; y--) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && !state.getFluidState().isSource()) {
                        MapColor mc = state.getMapColor(level, pos);
                        int rgb = mc.col; // or mc.color depending on mappings
                        r = (rgb >> 16) & 0xFF;
                        g = (rgb >> 8) & 0xFF;
                        b = rgb & 0xff;
                        a = 0xff;
                        break;
                    }
                }
                int index = i * 4;
//                WIMSMod.LogInfo(String.format("server pixel %s %s %s | %s %s %s %s", x - minX, (z - minZ), index, r, g, b, a));
                data[index] = (byte) r; // R
                data[index + 1] = (byte) g; // G
                data[index + 2] = (byte) b; // B
                data[index + 3] = (byte) a; // A
                i++;
            }
        }
        return data;
    }
}
