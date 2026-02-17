package org.valkyrienskies.wims;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public record ShipImagePacket(
        String slug,
        int width,
        int height,
        int dataLength,
        byte[] data
) {
    public static FriendlyByteBuf toBuffer(ShipImagePacket img) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(img.slug);
        buf.writeInt(img.width);
        buf.writeInt(img.height);
        buf.writeInt(img.data.length);
        buf.writeByteArray(img.data);
        return buf;
    }

    public static ShipImagePacket fromBuffer(FriendlyByteBuf buf) {
        String slug = buf.readUtf();
        int width = buf.readInt();
        int height = buf.readInt();
        int length = buf.readInt();
        byte[] data = buf.readByteArray(length);
        return new ShipImagePacket(
                slug,
                width,
                height,
                length,
                data
        );
    }
}
