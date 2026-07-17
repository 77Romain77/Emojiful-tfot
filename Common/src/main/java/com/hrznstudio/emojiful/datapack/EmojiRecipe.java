package com.hrznstudio.emojiful.datapack;

import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class EmojiRecipe implements Recipe<Container> {

    private final ResourceLocation recipeName;
    private final String category;
    private final String name;
    private final ResourceLocation texture;
    private final List<String> strings;
    private final List<String> texts;
    private final int sort;

    public EmojiRecipe(ResourceLocation recipeName, String category, String name, ResourceLocation texture,
                       List<String> strings, List<String> texts, int sort) {
        this.recipeName = recipeName;
        this.category = category;
        this.name = name;
        this.texture = texture;
        this.strings = List.copyOf(strings);
        this.texts = List.copyOf(texts);
        this.sort = sort;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return recipeName;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Services.PLATFORM.getRecipeSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return Services.PLATFORM.getRecipeType();
    }

    public ResourceLocation getRecipeName() {
        return recipeName;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public List<String> getStrings() {
        return strings;
    }

    public List<String> getTexts() {
        return texts;
    }

    public int getSort() {
        return sort;
    }
}
