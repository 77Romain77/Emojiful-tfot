package com.hrznstudio.emojiful.api;

import java.io.File;

/** An emoji loaded lazily from the local Minecraft game directory. */
public class EmojiFromFile extends Emoji {
    private final File imageFile;

    public EmojiFromFile(File imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    public File getCache() {
        return imageFile;
    }

    @Override
    public String getUrl() {
        return imageFile.getName().toLowerCase();
    }
}
