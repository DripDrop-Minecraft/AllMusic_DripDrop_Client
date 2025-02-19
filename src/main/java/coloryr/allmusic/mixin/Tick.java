package Coloryr.AllMusic.mixin;

import Coloryr.AllMusic.AllMusic;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class Tick {

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo info) {
        AllMusic.nowPlaying.tick();
    }
}
