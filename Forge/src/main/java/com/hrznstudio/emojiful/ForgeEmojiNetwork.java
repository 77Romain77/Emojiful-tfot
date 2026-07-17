package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Synchronizes the raw images referenced by emoji recipes in a server datapack. */
public final class ForgeEmojiNetwork {
    private static final String PROTOCOL = "2";
    private static final int CHUNK_SIZE = 512 * 1024;
    private static final AtomicInteger NEXT_SYNC_ID = new AtomicInteger();

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "datapack_textures"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private ForgeEmojiNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(EmojiTextureChunk.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EmojiTextureChunk::encode)
                .decoder(EmojiTextureChunk::decode)
                .consumerMainThread(EmojiTextureChunk::handle)
                .add();
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        MinecraftServer server = event.getPlayerList().getServer();
        byte[] bundle = createBundle(server);
        int syncId = NEXT_SYNC_ID.incrementAndGet();
        int chunkCount = Math.max(1, (bundle.length + CHUNK_SIZE - 1) / CHUNK_SIZE);

        List<ServerPlayer> players = event.getPlayer() == null
                ? event.getPlayerList().getPlayers()
                : List.of(event.getPlayer());
        for (ServerPlayer player : players) {
            for (int index = 0; index < chunkCount; index++) {
                int start = index * CHUNK_SIZE;
                int length = Math.min(CHUNK_SIZE, bundle.length - start);
                byte[] bytes = new byte[Math.max(0, length)];
                if (length > 0) System.arraycopy(bundle, start, bytes, 0, length);
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new EmojiTextureChunk(syncId, index, chunkCount, bytes));
            }
        }
    }

    private static byte[] createBundle(MinecraftServer server) {
        Map<ResourceLocation, byte[]> textures = new LinkedHashMap<>();
        for (EmojiRecipe recipe : server.getRecipeManager().getAllRecipesFor(EmojifulForge.EMOJI_RECIPE_TYPE.get())) {
            ResourceLocation location = recipe.getTexture();
            if (textures.containsKey(location)) continue;
            try {
                Resource resource = server.getResourceManager().getResource(location)
                        .orElseThrow(() -> new IOException("Missing datapack resource " + location));
                try (InputStream input = resource.open()) {
                    textures.put(location, input.readAllBytes());
                }
            } catch (IOException exception) {
                Constants.LOG.error("Unable to read emoji texture {} from the server datapack", location, exception);
            }
        }

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(textures.size());
                for (Map.Entry<ResourceLocation, byte[]> entry : textures.entrySet()) {
                    output.writeUTF(entry.getKey().toString());
                    output.writeInt(entry.getValue().length);
                    output.write(entry.getValue());
                }
            }
            Constants.LOG.info("Prepared {} datapack emoji textures ({} bytes)", textures.size(), bytes.size());
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to build the emoji texture bundle", exception);
        }
    }
}
