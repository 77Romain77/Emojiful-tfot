package com.hrznstudio.emojiful.api;

import com.hrznstudio.emojiful.ClientEmojiTextureCache;
import com.hrznstudio.emojiful.Constants;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** An emoji whose image is supplied by the connected server's datapack. */
public class EmojiFromDatapack extends Emoji {
    private final ResourceLocation texture;
    private File cache;

    public EmojiFromDatapack(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public void checkLoad() {
        if (cache == null) {
            byte[] image = ClientEmojiTextureCache.get(texture);
            if (image == null) return;

            cache = createCacheFile(image);
            try {
                if (!cache.isFile()) {
                    Files.createDirectories(cache.toPath().getParent());
                    Files.write(cache.toPath(), image);
                }
            } catch (IOException exception) {
                Constants.LOG.error("Unable to cache datapack emoji {}", texture, exception);
                finishedLoading = true;
                frames.add(error_texture);
                return;
            }
        }
        super.checkLoad();
    }

    @Override
    public File getCache() {
        return cache;
    }

    @Override
    public String getUrl() {
        return texture.getPath();
    }

    private File createCacheFile(byte[] image) {
        String safeName = (texture.getNamespace() + "_" + texture.getPath()).replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File("emojiful/datapack-cache/" + safeName + "-" + digest(image));
    }

    private static String digest(byte[] image) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(image);
            StringBuilder result = new StringBuilder(16);
            for (int i = 0; i < 8; i++) result.append(String.format("%02x", digest[i]));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
