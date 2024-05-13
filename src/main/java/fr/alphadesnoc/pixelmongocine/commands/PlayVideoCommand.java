package fr.alphadesnoc.pixelmongocine.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.alphadesnoc.pixelmongocine.network.PacketHandler;
import fr.alphadesnoc.pixelmongocine.network.message.SendVideoMessage;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;

public final class PlayVideoCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("playvideo")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("volume", IntegerArgumentType.integer(0, 100))
                                .then(Commands.argument("url", StringArgumentType.string())
                                        .executes(PlayVideoCommand::execute)))));
    }

    private static int execute(CommandContext<CommandSource> command) throws CommandSyntaxException {
        PlayerEntity targetPlayer = (PlayerEntity) EntityArgument.getEntity(command, "target");
        int volume = IntegerArgumentType.getInteger(command, "volume");
        String url = StringArgumentType.getString(command, "url");

        PacketHandler.sendTo(new SendVideoMessage(
                url,
                volume
        ), targetPlayer);
        return Command.SINGLE_SUCCESS;
    }
}
