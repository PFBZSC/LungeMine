package com.lungemine.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class LungeMineItem extends SwordItem {
    public static final int MAX_USAGE_DURATION = 100;//总时长
    public static final ResourceKey<DamageType> LUNGEMINE_TYPE =
            ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("lungemine", "lungemine"));
    public static final double RAY_DISTANCE = 2.0;

    public enum Status{
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
        super(tier, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    //右键按下时触发
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        level.playSound(null,player, ModRegistry.BANZAI.get() , SoundSource.PLAYERS,1.0F,1.0F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int usedDuration = getUseDuration(stack, entity) - timeLeft;

        if (usedDuration >= MAX_USAGE_DURATION) {
            this.releaseUsing(stack, level, player, 0);
            return;
        }

        if (!level.isClientSide) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                    1,
                    6, // Speed Level
                    false, false, false
            ));
        }

        //碰撞检测
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
                    if (!player.hasInfiniteMaterials()) {
                        stack.shrink(1);
                    }
                }
                return;
            }
        }

        //视线检测
        Vec3 eyePosition = player.getEyePosition();
        Vec3 traceEnd = eyePosition.add(look.x * RAY_DISTANCE, look.y * RAY_DISTANCE, look.z * RAY_DISTANCE);

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                eyePosition,
                traceEnd,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        );

        net.minecraft.world.phys.BlockHitResult blockHit = level.clip(context);


        if (blockHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            if (!level.isClientSide) {

                bomb(player, level,blockHit.getBlockPos().getCenter(),HitResult.Type.BLOCK);
                player.getCooldowns().addCooldown(this, 20 * 3);
                player.stopUsingItem();
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            if (player.isUsingItem()) {
                player.stopUsingItem();
                player.getCooldowns().addCooldown(this, 20 * 3);
            }

        }

    }

    //爆炸
    private void bomb(Player player, Level level, Vec3 pos, HitResult.Type type) {
        if (!level.isClientSide){
            level.explode(player, pos.x, pos.y+(0.5f*((type == HitResult.Type.ENTITY)?1:0)), pos.z, 4f, Level.ExplosionInteraction.BLOCK);
            net.minecraft.core.Registry<DamageType> damageRegistry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
            DamageSource customDamageSource = new DamageSource(damageRegistry.getHolderOrThrow(LUNGEMINE_TYPE),player);
            player.hurt(customDamageSource, 16.0f);

        }
    }


    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            Level level = player.level();
            // 确保只在服务端执行爆炸和消耗逻辑
            if (!level.isClientSide) {
                int count = stack.getDamageValue();
                float explosionChance = 0.01F + (Math.min(count, 10) * 0.029F);
                if (player.getRandom().nextFloat() < explosionChance) {
                    bomb(player, level, target.position(),HitResult.Type.ENTITY);
                    if (!player.hasInfiniteMaterials()) {
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
                bomb(player, level, pos.getCenter(),HitResult.Type.BLOCK);
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
        }
        return true;
    }
}