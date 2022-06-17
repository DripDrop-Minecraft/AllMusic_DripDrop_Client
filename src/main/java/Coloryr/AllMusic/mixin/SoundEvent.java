package coloryr.allmusic.mixin;

import coloryr.allmusic.AllMusic;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundEvent {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void Quit(SoundInstance soundInstance, CallbackInfo info) {
        if (AllMusic.isPlay) {
            SoundCategory data = soundInstance.getCategory();
            switch (data) {
                case RECORDS, MUSIC -> info.cancel();
            }
        }
    }
}
