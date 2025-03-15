package com.thunder.riftgate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TPProcedure {

    public static void execute(Entity commandUser, ServerLevel targetDimension, double x, double y, double z) {
        if (commandUser == null || targetDimension == null) {
            return;
        }

        // Find nearby entities within a 4-block radius
        List<Entity> entitiesToTeleport = findNearbyEntities(commandUser);

        // Apply visual effects BEFORE teleportation for all affected entities
        for (Entity entity : entitiesToTeleport) {
            applyPreTeleportEffects(entity);
        }

        // Delay teleportation slightly for effects to be visible
        commandUser.getLevel().getServer().execute(() -> {
            BlockPos safePos = findSafePosition(targetDimension, new BlockPos((int) x, (int) y, (int) z));

            // Apply wormhole distortion effect
            for (Entity entity : entitiesToTeleport) {
                applyWormholeDistortion(entity);
            }

            // Play teleportation sound
            targetDimension.playSound(null, safePos, SoundEvents.ENDERMAN_TELEPORT, commandUser.getSoundSource(), 1.0F, 1.0F);

            // Summon lightning at destination
            summonLightning(targetDimension, safePos);

            // Teleport all affected entities
            for (Entity entity : entitiesToTeleport) {
                teleportEntity(entity, targetDimension, safePos);
            }

            // Apply post-teleport effects
            for (Entity entity : entitiesToTeleport) {
                applyPostTeleportEffects(entity);
            }
        });
    }

    private static List<Entity> findNearbyEntities(Entity entity) {
        return entity.level().getEntities(entity, new AABB(
                entity.getX() - 4, entity.getY() - 2, entity.getZ() - 4,
                entity.getX() + 4, entity.getY() + 2, entity.getZ() + 4
        ));
    }

    private static void applyPreTeleportEffects(Entity entity) {
        ServerLevel world = (ServerLevel) entity.getLevel();

        // 1️⃣ Screen Fade to Black Effect
        if (entity instanceof Player player) {
            fadeScreen(player);
        }

        // 2️⃣ Floating Text (Holographic Grid Effect)
        if (entity instanceof Player player) {
            player.displayClientMessage(Component.literal("§b[Wormhole Engaged...]"), true);
        }

        // 3️⃣ Glowing Outline
        entity.setGlowing(true);

        // 4️⃣ Scanning Beam Effect (Conduit Power-like glow)
        if (entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 1, false, false));
        }

        // 5️⃣ Play futuristic sound effect
        world.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1.0F, 1.2F);
    }

    private static void applyPostTeleportEffects(Entity entity) {
        entity.setGlowing(false);
    }

    private static void applyWormholeDistortion(Entity entity) {
        if (entity instanceof Player player) {
            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false));
            player.connection.send(new ClientboundSoundPacket((Holder<SoundEvent>) SoundEvents.PORTAL_TRAVEL, player.getSoundSource(), player.getX(), player.getY(), player.getZ(), 1.0F, 0.5F, player.getRandom().nextLong()));
            spawnWormholeParticles(player);
        }
    }

    private static void spawnWormholeParticles(Entity entity) {
        ServerLevel world = (ServerLevel) entity.getLevel();
        for (int i = 0; i < 50; i++) {
            world.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(),
                    10,
                    0.5,
                    1.5,
                    0.5,
                    0.1
            );
        }
    }

    private static void fadeScreen(Player player) {
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§b[Entering Wormhole...]")));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("")));
        player.connection.send(new ClientboundSetTitleAnimationPacket(20, 60, 20)); // 1 sec fade in, 3 sec hold, 1 sec fade out
    }

    private static void summonLightning(ServerLevel world, BlockPos pos) {
        LightningBolt lightning = new LightningBolt(world);
        lightning.moveTo(Vec3.atBottomCenterOf(pos));
        world.addFreshEntity(lightning);
    }

    private static void teleportEntity(Entity entity, ServerLevel targetDimension, BlockPos pos) {
        if (entity.level() == targetDimension) {
            entity.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        } else {
            entity.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(
                    targetDimension,
                    new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                    Vec3.ZERO,
                    entity.getYRot(),
                    entity.getXRot(),
                    false,
                    net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING
            ));
        }
    }

    private static BlockPos findSafePosition(ServerLevel world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        while (!isSafeBlock(world.getBlockState(mutablePos)) && mutablePos.getY() < world.getMaxBuildHeight()) {
            mutablePos.move(0, 1, 0);
        }
        while (isSafeBlock(world.getBlockState(mutablePos.below())) && mutablePos.getY() > world.getMinBuildHeight()) {
            mutablePos.move(0, -1, 0);
        }
        return mutablePos;
    }

    private static boolean isSafeBlock(BlockState state) {
        return !state.isSolid() && !state.is(Blocks.LAVA) && !state.is(Blocks.FIRE);
    }
}