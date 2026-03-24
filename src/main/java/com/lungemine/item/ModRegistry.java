package com.lungemine.item;

import com.lungemine.LungeMine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab; // 新增：导入创造模式标签页类
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, LungeMine.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LungeMine.MODID);
    public static final RegistryObject<SoundEvent> BANZAI = registerSoundEvent("banzai");

    public static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(LungeMine.MODID, name)));
    }

    public static final RegistryObject<Item> LUNGE_MINE = ITEMS.register("lungemine",
            () -> new LungeMineItem(Tiers.WOOD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
        ITEMS.register(eventBus);
    }
}