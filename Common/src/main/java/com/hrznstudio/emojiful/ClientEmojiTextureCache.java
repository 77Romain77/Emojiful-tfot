package com.hrznstudio.emojiful;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Holds the image files sent by the server's datapacks for the active connection. */
public final class ClientEmojiTextureCache {
    private static volatile Map<ResourceLocation, byte[]> textures = Collections.emptyMap();

    private ClientEmojiTextureCache() {
    }

    public static byte[] get(ResourceLocation location) {
        return textures.get(location);
    }

    public static void replace(Map<ResourceLocation, byte[]> newTextures) {
        textures = Collections.unmodifiableMap(new HashMap<>(newTextures));
        Constants.LOG.info("Received {} emoji textures from the server datapack", textures.size());
    }

    public static void clear() {
        textures = Collections.emptyMap();
    }
}
