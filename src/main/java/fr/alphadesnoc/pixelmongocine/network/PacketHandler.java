package fr.alphadesnoc.pixelmongocine.network;

import fr.alphadesnoc.pixelmongocine.CineMod;
import fr.alphadesnoc.pixelmongocine.network.message.IMessage;
import fr.alphadesnoc.pixelmongocine.network.message.SendVideoMessage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketHandler {

    public static final String PROTOCOL_VERSION = "2";

    private static SimpleChannel INSTANCE;

    private static int nextId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(CineMod.MODID, "network"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        register(SendVideoMessage.class, new SendVideoMessage());
    }

    private static <T> void register(Class<T> clazz, IMessage<T> message) {
        INSTANCE.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
    }

    public static SimpleChannel getPlayChannel()
    {
        return INSTANCE;
    }

    public static <MSG> void sendTo(MSG msg, PlayerEntity player) {
        INSTANCE.sendTo(msg, ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToClient(MSG msg, Chunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }

    public static <MSG> void sendToAllTracking(MSG msg, LivingEntity entityToTrack) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entityToTrack), msg);
    }

    public static <MSG> void sendToAll(MSG msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

}
