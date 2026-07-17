package com.hrznstudio.emojiful.datapack;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class EmojiRecipeSerializer implements RecipeSerializer<EmojiRecipe> {


    public EmojiRecipeSerializer() {

    }

    @Override
    public EmojiRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String name = json.get("name").getAsString();
        List<String> strings = readStrings(json, "strings");
        if (strings.isEmpty()) strings.add(":" + name + ":");
        return new EmojiRecipe(recipeId, json.get("category").getAsString(), name,
                new ResourceLocation(json.get("texture").getAsString()), strings,
                readStrings(json, "texts"), json.has("sort") ? json.get("sort").getAsInt() : 0);
    }

    @Override
    public EmojiRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return new EmojiRecipe(recipeId, buffer.readUtf(), buffer.readUtf(), buffer.readResourceLocation(),
                buffer.readList(FriendlyByteBuf::readUtf), buffer.readList(FriendlyByteBuf::readUtf), buffer.readVarInt());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, EmojiRecipe recipe) {
        buffer.writeUtf(recipe.getCategory());
        buffer.writeUtf(recipe.getName());
        buffer.writeResourceLocation(recipe.getTexture());
        buffer.writeCollection(recipe.getStrings(), FriendlyByteBuf::writeUtf);
        buffer.writeCollection(recipe.getTexts(), FriendlyByteBuf::writeUtf);
        buffer.writeVarInt(recipe.getSort());
    }

    private static List<String> readStrings(JsonObject json, String key) {
        List<String> values = new ArrayList<>();
        if (!json.has(key)) return values;
        JsonArray array = json.getAsJsonArray(key);
        array.forEach(value -> values.add(value.getAsString()));
        return values;
    }

}
