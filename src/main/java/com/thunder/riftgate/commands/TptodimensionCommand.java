package com.thunder.riftgate.commands;

import com.mojang.brigadier.context.CommandContext;
import com.thunder.riftgate.TPProcedure;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class TptodimensionCommand {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("tptodimension")
                        .requires(source -> source.hasPermission(1)) // ✅ Changed to level 1 (ALL PLAYERS)
                        .then(Commands.argument("target_entity", EntityArgument.entity())
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> executeCommand(ctx, null)) // No coords, uses default spawn
                                        .then(Commands.argument("coords", BlockPosArgument.blockPos()) // Only coords
                                                .executes(ctx -> executeCommand(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "coords")))
                                        )
                                )
                        )
        );
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context, BlockPos targetPos) {
        try {
            CommandSourceStack source = context.getSource();
            Entity targetEntity = EntityArgument.getEntity(context, "target_entity");
            ServerLevel targetDimension = DimensionArgument.getDimension(context, "dimension");

            // If no coords are given, teleport to the dimension’s spawn point
            if (targetPos == null) {
                targetPos = targetDimension.getSharedSpawnPos();
            }

            // Send feedback message to player
            source.sendSuccess(() -> targetEntity.getName().copy()
                    .append(" §b[Initializing Wormhole...]"), true);

            // Call teleport procedure (which now handles chunk loading)
            TPProcedure.execute(targetEntity, targetDimension, targetPos.getX(), targetPos.getY(), targetPos.getZ());

        } catch (Exception e) {
            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("§cTeleportation failed: " + e.getMessage()));
        }
        return 1;
    }
}
