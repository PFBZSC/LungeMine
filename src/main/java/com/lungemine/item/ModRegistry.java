package com.lungemine.item;

import com.lungemine.LungeMine;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LungeMine.MODID);
    public static final DeferredItem<Item> LUNGE_MINE = ITEMS.register("lungemine",() -> new LungeMineItem(Tiers.WOOD, new Item.Properties().attributes(LungeMineItem.createAttributes(Tiers.WOOD, 3, -2.4F))));
    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT,LungeMine.MODID);
    public static final DeferredHolder<SoundEvent,SoundEvent> BANZAI = registerSoundEvent("banzai");

    public static DeferredHolder<SoundEvent,SoundEvent> registerSoundEvent(String name){
        return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(LungeMine.MODID,name)));
    }
    public static void register(IEventBus eventBus){
        SOUNDS.register(eventBus);
        ITEMS.register(eventBus);
    }


}
