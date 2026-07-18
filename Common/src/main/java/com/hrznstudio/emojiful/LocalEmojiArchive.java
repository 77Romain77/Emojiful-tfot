package com.hrznstudio.emojiful;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Keeps the local emoji ZIP open so lazy image loading does not re-index it for every texture. */
public final class LocalEmojiArchive {
    private static ZipFile archive;

    private LocalEmojiArchive() {
    }

    public static synchronized ZipFile open(Path path) throws IOException {
        if (archive != null) archive.close();
        archive = new ZipFile(path.toFile(), StandardCharsets.UTF_8);
        return archive;
    }

    public static synchronized boolean contains(String entryName) {
        return archive != null && archive.getEntry(entryName) != null;
    }

    public static synchronized InputStream openEntry(String entryName) throws IOException {
        if (archive == null) throw new IOException("Local emoji archive is not open");
        ZipEntry entry = archive.getEntry(entryName);
        if (entry == null) throw new IOException("Missing emoji ZIP entry: " + entryName);
        return archive.getInputStream(entry);
    }
}
