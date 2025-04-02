package com.thunder.riftgate.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.thunder.riftgate.TPProcedure;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class TptodimensionCommand {
    public TptodimensionCommand() {
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tptodimension").requires((source) -> source.hasPermission(4))).then(Commands.argument("target_entity", EntityArgument.entity()).then(((RequiredArgumentBuilder)Commands.argument("dimension", DimensionArgument.dimension()).executes((ctx) -> executeCommand(ctx, (BlockPos)null))).then(Commands.argument("coords", BlockPosArgument.blockPos()).executes((ctx) -> executeCommand(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "coords")))))));
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context, BlockPos targetPos) {
        try {
            CommandSourceStack source = (CommandSourceStack)context.getSource();
            Entity targetEntity = EntityArgument.getEntity(context, "target_entity");
            ServerLevel targetDimension = DimensionArgument.getDimension(context, "dimension");
            if (targetPos == null) {
                targetPos = targetDimension.getSharedSpawnPos();
            }

            TPProcedure.execute(targetEntity, targetDimension, (double)targetPos.getX(), (double)targetPos.getY(), (double)targetPos.getZ());
            source.sendSuccess(() -> targetEntity.getName().copy().append(" has been teleported!"), true);
        } catch (Exception e) {
            ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("Failed to execute teleport command: " + e.getMessage()));
        }

        return 1;
    }
}