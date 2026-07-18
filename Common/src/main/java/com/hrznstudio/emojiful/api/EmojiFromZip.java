package com.hrznstudio.emojiful.api;

import com.hrznstudio.emojiful.LocalEmojiArchive;
import java.io.IOException;
import java.io.InputStream;

/** An emoji loaded lazily from an entry in emojiful/emojis.zip. */
public class EmojiFromZip extends Emoji {
    private final String entryName;

    public EmojiFromZip(String entryName) {
        this.entryName = entryName;
    }

    @Override
    public String getUrl() {
        return entryName.toLowerCase();
    }

    @Override
    protected boolean imageExists() {
        return LocalEmojiArchive.contains(entryName);
    }

    @Override
    protected InputStream openImageStream() throws IOException {
        return LocalEmojiArchive.openEntry(entryName);
    }
}
