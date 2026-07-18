package com.hrznstudio.emojiful;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromFile;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/** Discovers client-only emojis from emojiful/emojis/<category>/<name>.png or .gif. */
public final class LocalEmojiLoader {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("png", "gif");

    private LocalEmojiLoader() {
    }

    public static void load() {
        Path root = Minecraft.getInstance().gameDirectory.toPath().resolve("emojiful").resolve("emojis");
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            Constants.LOG.error("Unable to create local emoji folder {}", root, exception);
            return;
        }

        Set<String> registeredAliases = new HashSet<>();
        int loaded = 0;
        for (Path categoryFolder : listDirectories(root)) {
            String categoryName = categoryFolder.getFileName().toString();
            List<Emoji> categoryEmojis = new ArrayList<>();
            int fallbackSort = 0;
            for (Path image : listImages(categoryFolder)) {
                String fileName = image.getFileName().toString();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String emojiName = normalizeName(baseName);
                if (emojiName.isEmpty()) {
                    Constants.LOG.warn("Ignoring local emoji with invalid filename: {}", image);
                    continue;
                }

                JsonObject metadata = readMetadata(image.resolveSibling(baseName + ".json"));
                EmojiFromFile emoji = new EmojiFromFile(image.toFile());
                emoji.name = metadata != null && metadata.has("name") ? metadata.get("name").getAsString() : emojiName;
                emoji.location = image.toAbsolutePath().toString();
                emoji.version = image.toAbsolutePath().normalize().toString().hashCode();
                emoji.sort = metadata != null && metadata.has("sort") ? metadata.get("sort").getAsInt() : fallbackSort;
                emoji.strings = readAliases(metadata, emojiName, registeredAliases);
                emoji.texts = readStrings(metadata, "texts");
                fallbackSort++;

                if (emoji.strings.isEmpty()) {
                    Constants.LOG.warn("Ignoring local emoji {} because all its aliases are already used", image);
                    continue;
                }
                categoryEmojis.add(emoji);
                Constants.EMOJI_LIST.add(emoji);
                if (!emoji.texts.isEmpty()) ClientEmojiHandler.EMOJI_WITH_TEXTS.add(emoji);
                loaded++;
            }

            if (!categoryEmojis.isEmpty()) {
                categoryEmojis.sort(Comparator.comparingInt(emoji -> emoji.sort));
                Constants.EMOJI_MAP.put(categoryName, categoryEmojis);
                ClientEmojiHandler.CATEGORIES.add(new EmojiCategory(categoryName, false));
            }
        }
        ClientEmojiHandler.EMOJI_WITH_TEXTS.sort(Comparator.comparingInt(emoji -> emoji.sort));
        Constants.LOG.info("Loaded {} local emojis from {}", loaded, root);
    }

    private static List<Path> listDirectories(Path root) {
        try (Stream<Path> paths = Files.list(root)) {
            return paths.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        } catch (IOException exception) {
            Constants.LOG.error("Unable to list local emoji categories in {}", root, exception);
            return List.of();
        }
    }

    private static List<Path> listImages(Path categoryFolder) {
        try (Stream<Path> paths = Files.list(categoryFolder)) {
            return paths.filter(Files::isRegularFile)
                    .filter(LocalEmojiLoader::isSupportedImage)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        } catch (IOException exception) {
            Constants.LOG.error("Unable to list local emojis in {}", categoryFolder, exception);
            return List.of();
        }
    }

    private static boolean isSupportedImage(Path path) {
        String name = path.getFileName().toString();
        int separator = name.lastIndexOf('.');
        return separator >= 0 && SUPPORTED_EXTENSIONS.contains(name.substring(separator + 1).toLowerCase(Locale.ROOT));
    }

    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT).trim().replace(' ', '_').replaceAll("[^a-z0-9_+\\-]", "_")
                .replaceAll("_+", "_");
    }

    private static JsonObject readMetadata(Path path) {
        if (!Files.isRegularFile(path)) return null;
        try (Reader reader = Files.newBufferedReader(path)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception exception) {
            Constants.LOG.warn("Ignoring invalid local emoji metadata {}", path, exception);
            return null;
        }
    }

    private static List<String> readAliases(JsonObject metadata, String defaultName, Set<String> registeredAliases) {
        List<String> aliases = metadata == null ? new ArrayList<>() : readStrings(metadata, "aliases");
        if (aliases.isEmpty()) aliases.add(defaultName);
        List<String> accepted = new ArrayList<>();
        for (String alias : aliases) {
            String wrapped = alias.startsWith(":") && alias.endsWith(":") ? alias : ":" + normalizeName(alias) + ":";
            String key = wrapped.toLowerCase(Locale.ROOT);
            if (wrapped.length() > 2 && registeredAliases.add(key)) accepted.add(wrapped);
        }
        return accepted;
    }

    private static List<String> readStrings(JsonObject metadata, String key) {
        List<String> values = new ArrayList<>();
        if (metadata == null || !metadata.has(key) || !metadata.get(key).isJsonArray()) return values;
        JsonArray array = metadata.getAsJsonArray(key);
        array.forEach(value -> values.add(value.getAsString()));
        return values;
    }
}
