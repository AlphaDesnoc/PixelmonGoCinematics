package fr.alphadesnoc.pixelmongocine.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.alphadesnoc.pixelmongocine.CineMod;
import fr.alphadesnoc.pixelmongocine.utils.MemoryTracker;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class VideoScreen extends Screen
{

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // STATUS
    int tick = 0;
    int closingOnTick = -1;
    float fadeLevel = 0;
    boolean started;
    boolean closing = false;
    boolean paused = false;
    float volume;
    // TOOLS
    private final SyncVideoPlayer player;

    // VIDEO INFO
    int videoTexture = -1;


    public VideoScreen(String url, int volume) {
        super(new StringTextComponent(""));

        Minecraft minecraft = Minecraft.getInstance();
        Minecraft.getInstance().getSoundHandler().pause();

        this.volume = volume;

        this.player = new SyncVideoPlayer(null, minecraft, MemoryTracker::create);

        player.setVolume((int) (Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.MASTER) * volume));
        player.start(url);
        started = true;
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
    }

    @Override
    public void render(MatrixStack stack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (!started) return;

        videoTexture = player.prepareTexture();

        if (player.isEnded() || player.isStopped() || player.getRawPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (pPartialTicks / 8), 0.0f);
                renderBlackBackground(stack);
                renderIcon(stack, ImageAPI.loadingGif());
                if (fadeLevel == 0) onClose();
                return;
            }
        }

        boolean playingState = (player.isPlaying() || player.isPaused()) && (player.getRawPlayerState().equals(State.PLAYING) || player.getRawPlayerState().equals(State.PAUSED));
        fadeLevel = (playingState) ? Math.max(fadeLevel - (pPartialTicks / 8), 0.0f) : Math.min(fadeLevel + (pPartialTicks / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(stack, videoTexture);
        }

        // BLACK SCREEN
        if (!paused)
            renderBlackBackground(stack);

        // RENDER GIF
        if (!player.isPlaying() || !player.getRawPlayerState().equals(State.PLAYING)) {
            if (player.isPaused() && player.getRawPlayerState().equals(State.PAUSED)) {
                renderIcon(stack, CineMod.pausedImage());
            } else {
                renderIcon(stack, ImageAPI.loadingGif());
            }
        }

        // DEBUG RENDERING
        if (!FMLLoader.isProduction()) {
            draw(stack, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(stack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(stack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderBlackBackground(MatrixStack stack) {
        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, WaterMediaAPI.math_colorARGB((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private void renderTexture(MatrixStack stack, int texture) {
        if (player.getDimensions() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, WaterMediaAPI.math_colorARGB(255, 0, 0, 0));
        RenderSystem.disableBlend();

        RenderSystem.bindTexture(texture);

        // Get video dimensions
        Dimension videoDimensions = player.getDimensions();
        double videoWidth = videoDimensions.getWidth();
        double videoHeight = videoDimensions.getHeight();

        // Calculate aspect ratios for both the screen and the video
        float screenAspectRatio = (float) width / height;
        float videoAspectRatio = (float) ((float) videoWidth / videoHeight);

        // New dimensions for rendering video texture
        int renderWidth, renderHeight;

        // If video's aspect ratio is greater than screen's, it means video's width needs to be scaled down to screen's width
        if(videoAspectRatio > screenAspectRatio) {
            renderWidth = width;
            renderHeight = (int) (width / videoAspectRatio);
        } else {
            renderWidth = (int) (height * videoAspectRatio);
            renderHeight = height;
        }

        int xOffset = (width - renderWidth) / 2; // xOffset for centering the video
        int yOffset = (height - renderHeight) / 2; // yOffset for centering the video

        RenderSystem.enableBlend();
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        AbstractGui.blit(stack, xOffset, yOffset, 0.0F, 0.0F, renderWidth, renderHeight, renderWidth, renderHeight);
        RenderSystem.disableBlend();
    }

    private void renderIcon(MatrixStack stack, ImageRenderer image) {
        RenderSystem.enableBlend();
        RenderSystem.bindTexture(image.texture(tick, 1, true));
        AbstractGui.blit(stack, width - 36, height - 36 , 0, 0, 36, 36, 28, 28);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void draw(MatrixStack stack, String text, int height) {
        drawString(stack, Minecraft.getInstance().fontRenderer, text, 5, height, 0xffffff);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {

        if(pKeyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
        }

        // Up arrow key (Volume)
        if (pKeyCode == GLFW.GLFW_KEY_UP) {
            if (volume <= 95) {
                volume += 5;
            } else {
                volume = 100;
                float masterVolume = Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.MASTER);
                if (masterVolume <= 0.95)
                    Minecraft.getInstance().gameSettings.setSoundLevel(SoundCategory.MASTER, masterVolume + 0.1F);
                else
                    Minecraft.getInstance().gameSettings.setSoundLevel(SoundCategory.MASTER, 1);
            }

            float actualVolume = Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.MASTER);
            float newVolume = volume * actualVolume;
            player.setVolume((int) newVolume);
        }

        // Down arrow key (Volume)
        if (pKeyCode == GLFW.GLFW_KEY_DOWN) {
            if (volume >= 5) {
                volume -= 5;
            } else {
                volume = 0;
            }
            float actualVolume = Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.MASTER);
            float newVolume = volume * actualVolume;
            player.setVolume((int) newVolume);
        }

        // Shift + Right arrow key (Forwards)
        if (pKeyCode == GLFW.GLFW_KEY_RIGHT) {
            player.seekTo(player.getTime() + 30000);
        }

        // Shift + Left arrow key (Backwards)
        if (pKeyCode == GLFW.GLFW_KEY_LEFT) {
            player.seekTo(player.getTime() - 10000);
        }

        if (pKeyCode == GLFW.GLFW_KEY_SPACE) {
            if (!player.isPaused()) {
                paused = true;
                player.pause();
            } else {
                paused = false;
                player.play();
            }
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (started) {
            started = false;
            player.stop();
            Minecraft.getInstance().getSoundHandler().resume();
            GlStateManager.deleteTexture(videoTexture);
            player.release();
            Minecraft.getInstance().displayGuiScreen(new IngameMenuScreen(true));
        }
    }

    @Override
    protected void init() {
        if (Minecraft.getInstance().currentScreen != null) {
            this.width = Minecraft.getInstance().currentScreen.width;
            this.height = Minecraft.getInstance().currentScreen.height;
        }
        super.init();
    }


}
