package com.lungemine;

import com.lungemine.item.LungeMineItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LungeMine.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LungeMineClient {

    //强制移动
    @SubscribeEvent
    public static void onInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

        if (player.isUsingItem() && player.getUseItem().getItem() instanceof LungeMineItem) {
            if (((LungeMineItem) player.getUseItem().getItem()).getStatus(player) == LungeMineItem.Status.RUN) {
                event.getInput().forwardImpulse = 5.0F;
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

            ItemTransforms.TransformType transformType = isRightHand ?
                    ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                    ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;

            poseStack.translate(0.4 * (isMainHand ? 1 : -1), -0.3, -0.4);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-60.0F));

            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
                    player,
                    stack,
                    transformType,
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