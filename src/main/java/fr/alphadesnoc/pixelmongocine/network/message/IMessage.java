package fr.alphadesnoc.pixelmongocine.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage<T>
{
    void encode(T message, ByteBuf buffer);

    T decode(ByteBuf buffer);

    void handle(T message, Supplier<NetworkEvent.Context> supplier);
}