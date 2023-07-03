//package we.devs.forever.mixin.mixins.client;
//
//import com.google.common.collect.Multimap;
//import net.minecraft.client.audio.ISound;
//import net.minecraft.client.audio.ITickableSound;
//import net.minecraft.client.audio.SoundManager;
//import net.minecraft.util.SoundCategory;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.Marker;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//@Mixin(SoundManager.class)
//public abstract class MixinSoundFixer {
//
//    @Shadow private int playTime;
//    @Shadow @Final private List<ITickableSound> tickableSounds;
//
//    @Shadow public abstract void stopSound(ISound sound);
//
//    @Shadow @Final private Map<ISound, String> invPlayingSounds;
//    @Shadow private SoundManager.SoundSystemStarterThread sndSystem;
//
//    @Shadow protected abstract float getClampedVolume(ISound soundIn);
//
//    @Shadow protected abstract float getClampedPitch(ISound soundIn);
//
//    @Shadow @Final private Map<String, ISound> playingSounds;
//    @Shadow @Final private Map<String, Integer> playingSoundsStopTime;
//    @Shadow @Final private Map<ISound, Integer> delayedSounds;
//    @Shadow @Final private static Logger LOGGER;
//    @Shadow @Final private static Marker LOG_MARKER;
//    @Shadow @Final private Multimap<SoundCategory, String> categorySounds;
//
//    @Shadow public abstract void playSound(ISound p_sound);
//
//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite
//    public void updateAllSounds() {
//        ++this.playTime;
//
//        for (ITickableSound itickablesound : this.tickableSounds) {
//            itickablesound.update();
//
//            if (itickablesound.isDonePlaying()) {
//                this.stopSound(itickablesound);
//            } else {
//                String s = this.invPlayingSounds.get(itickablesound);
//                this.sndSystem.setVolume(s, this.getClampedVolume(itickablesound));
//                this.sndSystem.setPitch(s, this.getClampedPitch(itickablesound));
//                this.sndSystem.setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(), itickablesound.getZPosF());
//            }
//        }
//
//        Iterator<Map.Entry<String, ISound>> iterator = this.playingSounds.entrySet().iterator();
//
//        while (iterator.hasNext()) {
//            Map.Entry<String, ISound> entry = (Map.Entry) iterator.next();
//            String s1 = entry.getKey();
//            ISound isound = entry.getValue();
//
//            if (!this.sndSystem.playing(s1)) {
//                int i = ((Integer) this.playingSoundsStopTime.get(s1)).intValue();
//
//                if (i <= this.playTime) {
//                    int j = isound.getRepeatDelay();
//
//                    if (isound.canRepeat() && j > 0) {
//                        this.delayedSounds.put(isound, Integer.valueOf(this.playTime + j));
//                    }
//
//                    iterator.remove();
//                    LOGGER.debug(LOG_MARKER, "Removed channel {} because it's not playing anymore", (Object) s1);
//                    this.sndSystem.removeSource(s1);
//                    this.playingSoundsStopTime.remove(s1);
//
//                    try {
//                        this.categorySounds.remove(isound.getCategory(), s1);
//                    } catch (RuntimeException var8) {
//                        ;
//                    }
//
//                    if (isound instanceof ITickableSound) {
//                        this.tickableSounds.remove(isound);
//                    }
//                }
//            }
//        }
//
//        Iterator<Map.Entry<ISound, Integer>> iterator1 = this.delayedSounds.entrySet().iterator();
//
//        while (iterator1.hasNext()) {
//            Map.Entry<ISound, Integer> entry1 = (Map.Entry) iterator1.next();
//
//            if (this.playTime >= ((Integer) entry1.getValue()).intValue()) {
//                ISound isound1 = entry1.getKey();
//
//                if (isound1 instanceof ITickableSound) {
//                    ((ITickableSound) isound1).update();
//                }
//
//                this.playSound(isound1);
//                iterator1.remove();
//            }
//        }
//    }
//}
