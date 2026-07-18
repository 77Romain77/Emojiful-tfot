package com.hrznstudio.emojiful;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromZip;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Discovers client-only emojis from emojiful/emojis.zip. */
public final class LocalEmojiLoader {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("png", "gif");

    private LocalEmojiLoader() {
    }

    public static void load() {
        Path emojifulFolder = Minecraft.getInstance().gameDirectory.toPath().resolve("emojiful");
        Path zipPath = emojifulFolder.resolve("emojis.zip");
        Path categoryConfig = emojifulFolder.resolve("categories.json");
        try {
            Files.createDirectories(emojifulFolder);
        } catch (IOException exception) {
            Constants.LOG.error("Unable to create local emoji folder {}", emojifulFolder, exception);
            return;
        }
        if (!Files.isRegularFile(zipPath)) {
            Constants.LOG.warn("No local emoji archive found at {}", zipPath);
            return;
        }

        Set<String> registeredAliases = new HashSet<>();
        int loaded = 0;
        try {
            ZipFile zip = LocalEmojiArchive.open(zipPath);
            Map<String, List<ZipEntry>> imagesByCategory = collectImages(zip);
            List<String> categories = orderCategories(imagesByCategory.keySet(), readCategoryOrder(categoryConfig, zip));
            writeDefaultCategoryConfig(categoryConfig, categories);

            for (String categoryName : categories) {
                List<Emoji> categoryEmojis = new ArrayList<>();
                List<ZipEntry> images = imagesByCategory.getOrDefault(categoryName, List.of());
                images.sort(Comparator.comparing(entry -> entry.getName().toLowerCase(Locale.ROOT)));
                int fallbackSort = 0;
                for (ZipEntry image : images) {
                    String fileName = image.getName().substring(image.getName().lastIndexOf('/') + 1);
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String emojiName = normalizeName(baseName);
                    if (emojiName.isEmpty()) continue;

                    JsonObject metadata = readMetadata(zip, categoryName + "/" + baseName + ".json");
                    EmojiFromZip emoji = new EmojiFromZip(image.getName());
                    emoji.name = metadata != null && metadata.has("name") ? metadata.get("name").getAsString() : emojiName;
                    emoji.location = image.getName();
                    emoji.version = (zipPath.toAbsolutePath().normalize() + "!" + image.getName()).hashCode();
                    emoji.sort = metadata != null && metadata.has("sort") ? metadata.get("sort").getAsInt() : fallbackSort;
                    emoji.strings = readAliases(metadata, emojiName, registeredAliases);
                    emoji.texts = readStrings(metadata, "texts");
                    fallbackSort++;
                    if (emoji.strings.isEmpty()) continue;

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
        } catch (IOException exception) {
            Constants.LOG.error("Unable to read local emoji archive {}", zipPath, exception);
            return;
        }
        ClientEmojiHandler.EMOJI_WITH_TEXTS.sort(Comparator.comparingInt(emoji -> emoji.sort));
        Constants.LOG.info("Loaded {} local emojis from {}", loaded, zipPath);
    }

    private static Map<String, List<ZipEntry>> collectImages(ZipFile zip) {
        Map<String, List<ZipEntry>> result = new LinkedHashMap<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory() || !isSupportedImage(entry.getName())) continue;
            int separator = entry.getName().indexOf('/');
            if (separator <= 0 || entry.getName().indexOf('/', separator + 1) >= 0) continue;
            String category = entry.getName().substring(0, separator);
            result.computeIfAbsent(category, ignored -> new ArrayList<>()).add(entry);
        }
        return result;
    }

    private static boolean isSupportedImage(String name) {
        int separator = name.lastIndexOf('.');
        return separator >= 0 && SUPPORTED_EXTENSIONS.contains(name.substring(separator + 1).toLowerCase(Locale.ROOT));
    }

    private static List<String> readCategoryOrder(Path externalConfig, ZipFile zip) {
        try {
            if (Files.isRegularFile(externalConfig)) {
                try (Reader reader = Files.newBufferedReader(externalConfig, StandardCharsets.UTF_8)) {
                    return parseCategoryOrder(reader);
                }
            }
            ZipEntry internalConfig = zip.getEntry("categories.json");
            if (internalConfig != null) {
                try (Reader reader = new InputStreamReader(zip.getInputStream(internalConfig), StandardCharsets.UTF_8)) {
                    return parseCategoryOrder(reader);
                }
            }
        } catch (Exception exception) {
            Constants.LOG.warn("Ignoring invalid category order configuration", exception);
        }
        return List.of();
    }

    private static List<String> parseCategoryOrder(Reader reader) {
        JsonElement root = new JsonParser().parse(reader);
        JsonArray array = root.isJsonArray() ? root.getAsJsonArray() : root.getAsJsonObject().getAsJsonArray("order");
        List<String> result = new ArrayList<>();
        if (array != null) array.forEach(value -> result.add(value.getAsString()));
        return result;
    }

    private static List<String> orderCategories(Set<String> available, List<String> requested) {
        Map<String, String> byLowercaseName = new HashMap<>();
        available.forEach(name -> byLowercaseName.put(name.toLowerCase(Locale.ROOT), name));
        List<String> result = new ArrayList<>();
        for (String name : requested) {
            String actual = byLowercaseName.get(name.toLowerCase(Locale.ROOT));
            if (actual != null && !result.contains(actual)) result.add(actual);
        }
        Collator french = Collator.getInstance(Locale.FRENCH);
        available.stream().filter(name -> !result.contains(name)).sorted(french).forEach(result::add);
        return result;
    }

    private static void writeDefaultCategoryConfig(Path config, List<String> categories) {
        if (Files.exists(config)) return;
        JsonObject root = new JsonObject();
        JsonArray order = new JsonArray();
        categories.forEach(order::add);
        root.add("order", order);
        try {
            Files.writeString(config, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            Constants.LOG.warn("Unable to create category order configuration {}", config, exception);
        }
    }

    private static JsonObject readMetadata(ZipFile zip, String entryName) {
        ZipEntry entry = zip.getEntry(entryName);
        if (entry == null) return null;
        try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (Exception exception) {
            Constants.LOG.warn("Ignoring invalid local emoji metadata {}", entryName, exception);
            return null;
        }
    }

    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT).trim().replace(' ', '_').replaceAll("[^a-z0-9_+\\-]", "_")
                .replaceAll("_+", "_");
    }

    private static List<String> readAliases(JsonObject metadata, String defaultName, Set<String> registeredAliases) {
        List<String> aliases = metadata == null ? new ArrayList<>() : readStrings(metadata, "aliases");
        if (aliases.isEmpty()) aliases.add(defaultName);
        List<String> accepted = new ArrayList<>();
        for (String alias : aliases) {
            String wrapped = alias.startsWith(":") && alias.endsWith(":") ? alias : ":" + normalizeName(alias) + ":";
            if (wrapped.length() > 2 && registeredAliases.add(wrapped.toLowerCase(Locale.ROOT))) accepted.add(wrapped);
        }
        return accepted;
    }

    private static List<String> readStrings(JsonObject metadata, String key) {
        List<String> values = new ArrayList<>();
        if (metadata == null || !metadata.has(key) || !metadata.get(key).isJsonArray()) return values;
        metadata.getAsJsonArray(key).forEach(value -> values.add(value.getAsString()));
        return values;
    }
}
