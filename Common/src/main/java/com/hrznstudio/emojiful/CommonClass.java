package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.api.Emoji;
import com.hrznstudio.emojiful.api.EmojiCategory;
import com.hrznstudio.emojiful.api.EmojiFromDatapack;
import com.hrznstudio.emojiful.datapack.EmojiRecipe;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class CommonClass {


    public static void onRecipesUpdated(RecipeManager manager) {
        ClientEmojiHandler.CATEGORIES.removeIf(EmojiCategory::worldBased);
        Constants.EMOJI_LIST.removeIf(Emoji::worldBased);
        Constants.EMOJI_MAP.values().forEach(emojis -> emojis.removeIf(Emoji::worldBased));
        Constants.EMOJI_MAP.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        ClientEmojiHandler.EMOJI_WITH_TEXTS.removeIf(Emoji::worldBased);
        if (Services.CONFIG.loadDatapack()) {
            RecipeType<EmojiRecipe> emojiRecipeRecipeType = Services.PLATFORM.getRecipeType();
            List<EmojiRecipe> emojiList = manager.getAllRecipesFor(emojiRecipeRecipeType);
            for (EmojiRecipe emojiRecipe : emojiList) {
                EmojiFromDatapack emoji = new EmojiFromDatapack(emojiRecipe.getTexture());
                emoji.name = emojiRecipe.getName();
                emoji.strings = new ArrayList<>(emojiRecipe.getStrings());
                if (emoji.strings.isEmpty()) {
                    emoji.strings.add(":" + emojiRecipe.getName() + ":");
                }
                emoji.texts = new ArrayList<>(emojiRecipe.getTexts());
                emoji.sort = emojiRecipe.getSort();
                emoji.version = emojiRecipe.getTexture().hashCode();
                emoji.location = emojiRecipe.getTexture().toString();
                emoji.worldBased = true;
                Constants.EMOJI_MAP.computeIfAbsent(emojiRecipe.getCategory(), s -> new ArrayList<>()).add(emoji);
                Constants.EMOJI_LIST.add(emoji);
                if (!emoji.texts.isEmpty()) {
                    ClientEmojiHandler.EMOJI_WITH_TEXTS.add(emoji);
                }
                if (ClientEmojiHandler.CATEGORIES.stream().noneMatch(emojiCategory -> emojiCategory.name().equalsIgnoreCase(emojiRecipe.getCategory().toLowerCase()))) {
                    ClientEmojiHandler.CATEGORIES.add(0, new EmojiCategory(emojiRecipe.getCategory(), true));
                }
            }
            ClientEmojiHandler.EMOJI_WITH_TEXTS.sort(Comparator.comparingInt(o -> o.sort));
            Constants.EMOJI_MAP.values().forEach(emojis -> emojis.sort(Comparator.comparingInt(o -> o.sort)));
            ClientEmojiHandler.indexEmojis();
        }
    }

    public static boolean shouldKeyBeIgnored(int keyCode){
        return keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT;
    }
}
