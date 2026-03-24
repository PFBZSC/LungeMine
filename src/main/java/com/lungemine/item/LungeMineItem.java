package com.lungemine.item;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class LungeMineItem extends SwordItem {
    public static final int MAX_USAGE_DURATION = 100;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final double RAY_DISTANCE = 3.0;
    public enum Status {
        NONE,
        RUN
    }

    public Status getStatus(LivingEntity entity) {
        if (!entity.isUsingItem() || !(entity.getUseItem().getItem() instanceof LungeMineItem)) {
            return Status.NONE;
        }
        return Status.RUN;
    }

    public LungeMineItem(Tier tier, Properties properties) {
        super(tier, 3, -2.4F, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    //右键按下时触发
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        level.playSound(null, player,ModRegistry.BANZAI.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.consume(stack);
    }

    //onUseTick
    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        Level level = player.level;

        int usedDuration = getUseDuration(stack) - timeLeft;

        if (usedDuration >= MAX_USAGE_DURATION) {
            player.releaseUsingItem();
            return;
        }

        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    1,
                    6,
                    false, false, false
            ));
        }

        // 碰撞检测
        double expandLR = 0.5, expandForward = 2.0;
        Vec3 look = player.getLookAngle().normalize();
        AABB originalBox = player.getBoundingBox();
        AABB detectBox = new AABB(
                originalBox.getCenter().x - (-look.z) * expandLR,
                originalBox.minY,
                originalBox.getCenter().z - look.x * expandLR,
                originalBox.getCenter().x + (-look.z) * expandLR + look.x * expandForward,
                originalBox.maxY,
                originalBox.getCenter().z + look.x * expandLR + look.z * expandForward
        );

        for (LivingEntity entityHit : level.getEntitiesOfClass(LivingEntity.class, detectBox)) {
            if (entityHit != player && entityHit.isAlive()) {
                if (!level.isClientSide) {
                    bomb(player, level, entityHit.position(),HitResult.Type.ENTITY);
                    player.getCooldowns().addCooldown(this, 20 * 3);
                    player.stopUsingItem();
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return;
            }
        }


        //视线检测
        Vec3 eyePosition = player.getEyePosition();
        Vec3 traceEnd = eyePosition.add(look.x * RAY_DISTANCE, look.y * RAY_DISTANCE, look.z * RAY_DISTANCE);

        ClipContext context = new ClipContext(
                eyePosition,
                traceEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult blockHit = level.clip(context);

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            if (!level.isClientSide) {

                LOGGER.info(String.valueOf(blockHit.getLocation()));
                bomb(player, level, blockHit.getLocation(), HitResult.Type.BLOCK);

                player.getCooldowns().addCooldown(this, 20 * 3);
                player.stopUsingItem();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, 20 * 3);
        }
    }

    //爆炸
    private void bomb(Player player, Level level, Vec3 pos, HitResult.Type type) {
        if (!level.isClientSide){
            level.explode(player, pos.x, pos.y+(0.5f*((type == HitResult.Type.ENTITY)?1:0)), pos.z, 4f, Explosion.BlockInteraction.DESTROY);
            DamageSource customDamageSource = new DamageSource("lungemine").setExplosion();
            player.hurt(customDamageSource, 16.0f);

        }
    }


    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            stack.hurtAndBreak(1, attacker, (entity) -> {
                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
            Level level = player.level;
            if (!level.isClientSide) {
                int count = stack.getDamageValue();
                float explosionChance = 0.01F + (Math.min(count, 10) * 0.029F);
                if (player.getRandom().nextFloat() < explosionChance) {
                    bomb(player, level, target.position(),HitResult.Type.ENTITY);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (miningEntity instanceof Player player) {
            if (!level.isClientSide) {
                bomb(player, level, new Vec3(pos.getX(), pos.getY(), pos.getZ()),HitResult.Type.BLOCK);
                LOGGER.info(miningEntity.toString());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return true;
    }
}