package com.hrznstudio.emojiful;

import com.hrznstudio.emojiful.platform.ForgeConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class EmojifulForge {

    public EmojifulForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHelper.setup(new ForgeConfigSpec.Builder()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleClientSetup);
    }

    private void handleClientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ForgeClientHandler::hijackScreen);
    }

}
