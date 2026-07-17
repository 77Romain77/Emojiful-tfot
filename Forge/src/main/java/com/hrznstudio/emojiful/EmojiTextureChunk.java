package com.hrznstudio.emojiful;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record EmojiTextureChunk(int syncId, int index, int total, byte[] bytes) {
    private static final int MAX_CHUNK_SIZE = 512 * 1024;
    private static final int MAX_CHUNKS = 512;
    private static final int MAX_TEXTURES = 20_000;
    private static final int MAX_TEXTURE_SIZE = 16 * 1024 * 1024;

    private static int activeSyncId;
    private static byte[][] pendingChunks;
    private static int receivedChunks;

    public static void encode(EmojiTextureChunk message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.syncId);
        buffer.writeVarInt(message.index);
        buffer.writeVarInt(message.total);
        buffer.writeByteArray(message.bytes);
    }

    public static EmojiTextureChunk decode(FriendlyByteBuf buffer) {
        return new EmojiTextureChunk(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readByteArray(MAX_CHUNK_SIZE));
    }

    public static void handle(EmojiTextureChunk message, Supplier<NetworkEvent.Context> contextSupplier) {
        accept(message);
        contextSupplier.get().setPacketHandled(true);
    }

    private static void accept(EmojiTextureChunk message) {
        if (message.total < 1 || message.total > MAX_CHUNKS || message.index < 0 || message.index >= message.total) {
            Constants.LOG.error("Rejected invalid datapack emoji texture chunk");
            return;
        }
        if (pendingChunks == null || activeSyncId != message.syncId || pendingChunks.length != message.total) {
            activeSyncId = message.syncId;
            pendingChunks = new byte[message.total][];
            receivedChunks = 0;
            ClientEmojiTextureCache.clear();
        }
        if (pendingChunks[message.index] == null) {
            pendingChunks[message.index] = message.bytes;
            receivedChunks++;
        }
        if (receivedChunks != pendingChunks.length) return;

        try {
            ByteArrayOutputStream joined = new ByteArrayOutputStream();
            for (byte[] chunk : pendingChunks) joined.write(chunk);
            ClientEmojiTextureCache.replace(readBundle(joined.toByteArray()));
        } catch (IOException exception) {
            Constants.LOG.error("Unable to decode datapack emoji textures", exception);
            ClientEmojiTextureCache.clear();
        } finally {
            pendingChunks = null;
            receivedChunks = 0;
        }
    }

    private static Map<ResourceLocation, byte[]> readBundle(byte[] bundle) throws IOException {
        Map<ResourceLocation, byte[]> textures = new HashMap<>();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bundle))) {
            int count = input.readInt();
            if (count < 0 || count > MAX_TEXTURES) throw new IOException("Invalid texture count: " + count);
            for (int i = 0; i < count; i++) {
                ResourceLocation location = new ResourceLocation(input.readUTF());
                int length = input.readInt();
                if (length < 0 || length > MAX_TEXTURE_SIZE) throw new IOException("Invalid texture size: " + length);
                textures.put(location, input.readNBytes(length));
                if (textures.get(location).length != length) throw new IOException("Truncated texture " + location);
            }
        }
        return textures;
    }
}
