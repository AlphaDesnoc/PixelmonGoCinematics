package fr.alphadesnoc.pixelmongocine.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import fr.alphadesnoc.pixelmongocine.client.ClientHandler;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SendVideoMessage implements IMessage<SendVideoMessage> {

    private String url;
    private int volume;

    public SendVideoMessage() {}

    public SendVideoMessage(String url, int volume) {
        this.url = url;
        this.volume = volume;
    }

    @Override
    public void encode(SendVideoMessage message, ByteBuf buffer) {
        buffer.writeInt(message.url.length());
        buffer.writeCharSequence(message.url, StandardCharsets.UTF_8);
        buffer.writeInt(message.volume);
    }

    @Override
    public SendVideoMessage decode(ByteBuf buffer) {
        int urlLength = buffer.readInt();
        String url = String.valueOf(buffer.readCharSequence(urlLength, StandardCharsets.UTF_8));
        int volume = buffer.readInt();
        return new SendVideoMessage(url, volume);
    }

    @Override
    public void handle(SendVideoMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientHandler.openVideo(message.url, message.volume));
        supplier.get().setPacketHandled(true);
    }
}