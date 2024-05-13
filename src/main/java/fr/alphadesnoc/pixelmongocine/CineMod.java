package fr.alphadesnoc.pixelmongocine;

import fr.alphadesnoc.pixelmongocine.commands.RegisterCommands;
import fr.alphadesnoc.pixelmongocine.common.CommonHandler;
import fr.alphadesnoc.pixelmongocine.utils.cache.TextureCache;
import fr.alphadesnoc.pixelmongocine.utils.displayers.VideoDisplayer;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.core.tools.JarTool;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CineMod.MODID)
public final class CineMod {

    @OnlyIn(Dist.CLIENT)
    private static ImageRenderer IMG_PAUSED;

    @OnlyIn(Dist.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    public static final String MODID = "pixelmongocinematic";

    public CineMod() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(CommonHandler::setup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {

        IMG_PAUSED = ImageAPI.renderer(JarTool.readImage(CineMod.class.getClassLoader(), "/pictures/paused.png"), true);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        
        @SubscribeEvent
        public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                TextureCache.renderTick();
            }
        }

        @SubscribeEvent
        public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                TextureCache.clientTick();
                VideoDisplayer.tick();
            }
        }

        @SubscribeEvent
        public static void onUnloadingLevel(WorldEvent.Unload unload) {
            if (unload.getWorld() != null && unload.getWorld().isRemote()) {
                TextureCache.unload();
                VideoDisplayer.unload();
            }
        }
    }

}
