package com.lungemine;

import com.lungemine.item.ModRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LungeMine.MODID)
public class LungeMine
{
    public static final String MODID = "lungemine";

    public static final Logger LOGGER = LogUtils.getLogger();

    public LungeMine()
    {
        LOGGER.info("LungeMine is initializing...");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("LungeMine common setup complete.");
    }
}