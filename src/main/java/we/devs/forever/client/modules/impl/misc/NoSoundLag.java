package we.devs.forever.client.modules.impl.misc;

import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.ArrayList;
import java.util.Set;

public
class NoSoundLag
        extends Module {
    private static final Set<SoundEvent> BLACKLIST;
    private static NoSoundLag instance;

    static {
        BLACKLIST = Sets.newHashSet(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);
    }

    public Setting<Boolean> crystals = (new Setting<>("Crystals", true));
    public Setting<Boolean> armor = (new Setting<>("Armor", true));
    public Setting<Float> soundRange = (new Setting<>("SoundRange", Float.valueOf(12.0f), Float.valueOf(0.0f), Float.valueOf(12.0f)));

    public NoSoundLag() {
        super("NoSoundLag", "Prevents Lag through sound spam.", Category.MISC);
        instance = this;
    }

    public static void removeEntities(SPacketSoundEffect packet, float range) {
        BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
        ArrayList<Entity> toRemove = new ArrayList<Entity>();
        for (Entity entity : NoSoundLag.mc.world.loadedEntityList) {
            if (!(entity instanceof EntityEnderCrystal) || !(entity.getDistanceSq(pos) <= MathUtil.square(range)))
                continue;
            toRemove.add(entity);
        }
        for (Entity entity : toRemove) {
            entity.setDead();
        }
    }

    @EventListener
    public void onPacketReceived(PacketEvent.Receive event) {
//        if (event != null && event.getPacket() != null && NoSoundLag.mc.player != null && NoSoundLag.mc.world != null && event.getPacket() instanceof SPacketSoundEffect) {
//            SPacketSoundEffect packet = event.getPacket();
//            if (this.crystals.getValue() && packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE && (AutoCrystal.getInstance().isDisabled() || !AutoCrystal.getInstance().sound.getValue() && AutoCrystal.getInstance().threadMode.getValue() != ThreadMode.SOUND)) {
//                NoSoundLag.removeEntities(packet, this.soundRange.getValue());
//            }
//            if (BLACKLIST.contains(packet.getSound()) && this.armor.getValue()) {
//                event.setCanceled(true);
//            }
//        }
    }
}
