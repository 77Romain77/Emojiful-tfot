package com.hrznstudio.emojiful.mixin;

import com.hrznstudio.emojiful.gui.EmojiFontEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EditBox.class)
public abstract class EditBoxEmojifulMixin implements EmojiFontEditBox {

    @Shadow
    @Final
    @Mutable
    private Font font;

    @Override
    public void emojiful$setFont(Font font) {
        this.font = font;
    }
}
