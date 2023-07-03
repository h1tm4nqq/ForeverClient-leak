
/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.init.MobEffects
 *  net.minecraft.network.play.client.CPacketAnimation
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.EnumHand
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package we.devs.forever.client.modules.impl.render;


import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class Swings
        extends Module {
    private final Setting<Mode> mode = (new Setting<Mode>("OldAnimations", Mode.OneDotEight));
    private final Setting<Swing> swing = (new Setting<Swing>("Swing", Swing.Mainhand));
    public final Setting<Boolean> slow = (new Setting<>("Slow", false));
    public final Setting<Integer> slowSpeed = (new Setting<>("SlowSpeed", 15,1,30));
    public static Swings swings;
    public Swings() {
        super("Swings", "Change animations.", Category.RENDER);
        swings = this;
    }

    @Override
    public void onUpdate() {
        if (Swings.nullCheck()) {
            return;
        }
        if (this.swing.getValue() == Swing.Offhand) {
            Swings.mc.player.swingingHand = EnumHand.OFF_HAND;
        }
        if (this.swing.getValue() == Swing.Mainhand) {
            Swings.mc.player.swingingHand = EnumHand.MAIN_HAND;
        }
        if (this.mode.getValue() == Mode.OneDotEight && (double) Swings.mc.entityRenderer.itemRenderer.prevEquippedProgressMainHand >= 0.9) {
            Swings.mc.entityRenderer.itemRenderer.equippedProgressMainHand = 1.0f;
            Swings.mc.entityRenderer.itemRenderer.itemStackMainHand = Swings.mc.player.getHeldItemMainhand();
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send send) {
        Object t = send.getPacket();
        if (t instanceof CPacketAnimation && this.swing.getValue() == Swing.Disable) {
            send.cancel();
        }
    }

    private enum Swing {
        Mainhand,
        Offhand,
        Disable,
        None

    }

    private enum Mode {
        Normal,
        OneDotEight

    }
}

