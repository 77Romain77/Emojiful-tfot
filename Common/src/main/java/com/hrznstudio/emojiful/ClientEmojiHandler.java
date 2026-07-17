package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.render.EmojiFontRenderer;
import com.hrznstudio.emojiful.util.ProfanityFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.*;
import java.util.stream.Collectors;

public class ClientEmojiHandler {
    public static final List<EmojiCategory> CATEGORIES = new ArrayList<>();
    public static Font oldFontRenderer;
    public static List<String> ALL_EMOJIS = new ArrayList<>();
    public static HashMap<EmojiCategory, List<Emoji[]>> SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
    public static List<Emoji> EMOJI_WITH_TEXTS = new ArrayList<>();
    public static int lineAmount;

    public static void setup() {
        preInitEmojis();
        initEmojis();
        indexEmojis();
        Constants.LOG.info("Loaded " + Constants.EMOJI_LIST.size() + " emojis");
    }

    public static void indexEmojis() {
        lineAmount = 0;
        ALL_EMOJIS = Constants.EMOJI_LIST.stream().map(emoji -> emoji.strings).flatMap(Collection::stream).collect(Collectors.toList());
        SORTED_EMOJIS_FOR_SELECTION = new LinkedHashMap<>();
        for (EmojiCategory category : CATEGORIES) {
            List<Emoji> categoryEmojis = Constants.EMOJI_MAP.getOrDefault(category.name(), Collections.emptyList());
            if (categoryEmojis.isEmpty()) continue;

            List<Emoji[]> lines = new ArrayList<>();
            Emoji[] array = new Emoji[9];
            int i = 0;
            for (Emoji emoji : categoryEmojis) {
                array[i] = emoji;
                ++i;
                if (i >= array.length) {
                    lines.add(array);
                    array = new Emoji[9];
                    i = 0;
                }
            }
            if (i > 0) {
                lines.add(array);
            }
            SORTED_EMOJIS_FOR_SELECTION.put(category, lines);
            lineAmount += 1 + lines.size();
        }
    }

    private static void preInitEmojis() {
        CATEGORIES.addAll(Arrays.asList("Smileys & Emotion", "Animals & Nature", "Food & Drink", "Activities", "Travel & Places", "Objects", "Symbols", "Flags").stream().map(s -> new EmojiCategory(s, false)).collect(Collectors.toList()));
        if (com.hrznstudio.emojiful.platform.Services.CONFIG.getProfanityFilter()) ProfanityFilter.loadConfigs();
    }

    private static void initEmojis() {
        if (!Constants.error) {
            oldFontRenderer = Minecraft.getInstance().font;
            Minecraft.getInstance().font = new EmojiFontRenderer(Minecraft.getInstance().font);
            Minecraft.getInstance().getEntityRenderDispatcher().font = Minecraft.getInstance().font;
            BlockEntityRenderers.register(BlockEntityType.SIGN, p_173571_ -> {
                SignRenderer signRenderer = new SignRenderer(p_173571_);
                signRenderer.font = Minecraft.getInstance().font;
                return signRenderer;
            });
        }
    }

}
