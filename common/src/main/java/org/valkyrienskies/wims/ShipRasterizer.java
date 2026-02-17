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

//        NativeImage img = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        byte[] data = new byte[width * height * 4];

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int rgba = 0x00000000;
        for (int z = minZ; z < maxZ; z++) {
            for (int x = minX; x < maxX; x++) {
                rgba = 0x00000000;
                for (int y = maxY; y >= minY; y--) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && !state.getFluidState().isSource()) {
                        MapColor mc = state.getMapColor(level, pos);
                        int rgb = mc.col; // or mc.color depending on mappings
                        rgba = 0xFF000000 | (rgb & 0x00FFFFFF);
                        break;
                    }
                }
//                img.setPixelRGBA(x - minX, z - minZ, rgba);
                int index = ((z - minZ) * width + (x - minX)) * 4;
                data[index] = (byte) ((rgba >> 16) & 0xFF); // R
                data[index + 1] = (byte) ((rgba >> 8) & 0xFF); // G
                data[index + 2] = (byte) ((rgba) & 0xFF); // B
                data[index + 3] = (byte) ((rgba >> 24) & 0xFF); // A
            }
        }
        return deflate(data);
    }

    private static byte[] deflate(byte[] raw) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DeflaterOutputStream def = new DeflaterOutputStream(baos)) {
                def.write(raw);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
