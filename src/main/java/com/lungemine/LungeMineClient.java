package com.lungemine;

import com.lungemine.item.LungeMineItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;


@Mod(value = LungeMine.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LungeMine.MODID, value = Dist.CLIENT)
public class LungeMineClient {

    public LungeMineClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    //强制移动
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof LungeMineItem) {
            if (((LungeMineItem) player.getUseItem().getItem()).getStatus(player) == LungeMineItem.Status.RUN){
                event.getInput().forwardImpulse = 5.0F;// 使用物品是强制减少80%移速，5*0.2还原正常移速1.0
                event.getInput().up = true;
                event.getInput().leftImpulse = 0.0F;
                event.getInput().left = false;
                event.getInput().right = false;
                player.setSprinting(true);
            }
        }
    }

    //第一人称使用动作
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof LungeMineItem && player.isUsingItem() && player.getUseItem() == stack) {

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
            boolean isRightHand = (player.getMainArm() == HumanoidArm.RIGHT && isMainHand) ||
                    (player.getMainArm() == HumanoidArm.LEFT && !isMainHand);

            ItemDisplayContext displayContext = isRightHand ?
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND :
                    ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

            poseStack.translate(0.4*(isMainHand?1:-1),-0.3,-0.4);// 武器位置
            poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F)); // 稍微下倾

            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
                    player,
                    stack,
                    displayContext,
                    !isRightHand,
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight()
            );

            poseStack.popPose();
            event.setCanceled(true);
        }
    }

}