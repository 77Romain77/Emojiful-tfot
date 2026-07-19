package com.hrznstudio.emojiful.gui;


import com.hrznstudio.emojiful.CommonClass;
import com.hrznstudio.emojiful.Constants;
import com.hrznstudio.emojiful.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;

public class EmojifulBedChatScreen extends InBedChatScreen {

    private EmojiSelectionGui emojiSelectionGui;
    private EmojiSuggestionHelper emojiSuggestionHelper;

    @Override
    protected void init() {
        super.init();
        ((EmojiFontEditBox) this.input).emojiful$setFont(Minecraft.getInstance().font);
        if (!Constants.error) {
            if (Services.CONFIG.showEmojiAutocomplete()) emojiSuggestionHelper = new EmojiSuggestionHelper(this);
            if (Services.CONFIG.showEmojiSelector()) emojiSelectionGui = new EmojiSelectionGui(this);
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int x, int j, float partialTick) {
        super.render(guiGraphics, x, j, partialTick);
        if (emojiSuggestionHelper != null) emojiSuggestionHelper.render(guiGraphics);
        if (emojiSelectionGui != null) {
            emojiSelectionGui.mouseMoved(x, j);
            emojiSelectionGui.render(guiGraphics);
        }

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (emojiSelectionGui != null && emojiSelectionGui.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers) && CommonClass.shouldKeyBeIgnored(keyCode)){
            return true;
        }
        if (emojiSuggestionHelper != null && emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollDelta) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseScrolled(x, y, scrollDelta)) {
            return true;
        }
        return super.mouseScrolled(x, y, scrollDelta);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseClicked(x, y, button)) {
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseDragged(x, y, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(x, y, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (emojiSelectionGui != null && emojiSelectionGui.mouseReleased(x, y, button)) {
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (emojiSelectionGui != null && emojiSelectionGui.charTyped(c, i)) {
            return true;
        }
        return super.charTyped(c, i);
    }

}
