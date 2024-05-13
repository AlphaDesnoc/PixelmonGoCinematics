package fr.alphadesnoc.pixelmongocine.client;

import fr.alphadesnoc.pixelmongocine.client.gui.VideoScreen;
import net.minecraft.client.Minecraft;

public final class ClientHandler {

    public static void openVideo(String url, int volume) {
        Minecraft.getInstance().displayGuiScreen(new VideoScreen(url, volume));
    }

}
