package Coloryr.AllMusic;

import Coloryr.AllMusic.Hud.HudUtils;
import Coloryr.AllMusic.player.APlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllMusic implements ModInitializer {
    public static final Identifier ID = new Identifier("allmusic", "channel");
    public static APlayer nowPlaying;
    public static boolean isPlay = false;
    public static HudUtils hudUtils;

    public static void onServerQuit() {
        try {
            nowPlaying.close();
            nowPlaying.closePlayer();
            hudUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hudUtils.Lyric = hudUtils.Info = hudUtils.List = "";
        hudUtils.save = null;
    }

    public static void onClicentPacket(final String message) {
        new Thread(() -> {
            try {
                if (message.equals("[Stop]")) {
                    stopPlaying();
                } else if (message.startsWith("[Play]")) {
                    MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.MUSIC);
                    MinecraftClient.getInstance().getSoundManager().stopSounds(null, SoundCategory.RECORDS);
                    stopPlaying();
                    nowPlaying.setMusic(message.replace("[Play]", ""));
                } else if (message.startsWith("[Lyric]")) {
                    hudUtils.Lyric = message.substring(7);
                } else if (message.startsWith("[Info]")) {
                    hudUtils.Info = message.substring(6);
                } else if (message.startsWith("[List]")) {
                    hudUtils.List = message.substring(6);
                } else if (message.startsWith("[Pos]")) {
                    nowPlaying.set(message.substring(5));
                } else if (message.equalsIgnoreCase("[clear]")) {
                    hudUtils.close();
                } else if (message.startsWith("{")) {
                    hudUtils.setPos(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "allmusic").start();
    }

    private static void stopPlaying() {
        try {
            nowPlaying.close();
            hudUtils.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawText(String item, float x, float y){
        var hud = MinecraftClient.getInstance().textRenderer;
        hud.draw(item, x, y, 0xFFFFFF, false, new Matrix4f(),
                layer -> null,
                TextRenderer.TextLayerType.NORMAL,
                0x00FFFFFF,
                0x00FFFFFF);
    }

    public static float getVolume(){
        return MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS);
    }

    @Override
    public void onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buffer, responseSender) -> {
            try {
                byte[] buff = new byte[buffer.readableBytes()];
                buffer.readBytes(buff);
                buff[0] = 0;
                String data = new String(buff, StandardCharsets.UTF_8).substring(1);
                onClicentPacket(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        nowPlaying = new APlayer();
        hudUtils = new HudUtils();
    }
}
