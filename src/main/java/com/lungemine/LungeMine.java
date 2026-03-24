package com.lungemine;

import com.lungemine.item.ModRegistry;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(LungeMine.MODID)
public class LungeMine {
    public static final String MODID = "lungemine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LungeMine(IEventBus modEventBus) {
        LOGGER.info("LungeMine is initializing...");
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        ModRegistry.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("LungeMine common setup complete.");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT){
            event.accept(ModRegistry.LUNGE_MINE);
        }

    }
}
