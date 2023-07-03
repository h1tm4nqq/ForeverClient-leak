package we.devs.forever.api.util.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import we.devs.forever.api.event.events.client.TickEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.network.WorldClientEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.util.ListenedUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
* Util class help with adaptive modules to NCP anti-cheat
*/
public class NCPUtil extends ListenedUtil {
    private final AtomicLong lagTimer = new AtomicLong();
    private final TimerUtil clickTimer = new TimerUtil();
    private boolean endedSprint;
    private boolean endedSneak;
    private boolean windowClicks;
    private boolean strict;
    private volatile boolean sneaking;
    private volatile boolean sprinting;
    public int ticks = 0;

    public NCPUtil() {
        load();
    }

//    @EventListener(priority = ListenerPriority.HIGHEST)
//    public void onPacketReceive(PacketEvent.Receive event) {
//        if (event.getPacket() instanceof SPacketPlayerPosLook) {
//            this.lagTimer.set(System.currentTimeMillis());
//        }
//    }

    @EventListener(priority = ListenerPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck()) return;

        if (event.getPacket() instanceof CPacketClickWindow) {
            if (!this.isStrict() || event.isCanceled()) {
                return;
            }
//        if (mc.player.isActiveItemStackBlocking()) {
//            Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> mc.playerController.onStoppedUsingItem((EntityPlayer)mc.player));
//        }
            if (sneaking) {
                this.endedSneak = true;
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            if (sprinting) {
                this.endedSprint = true;
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
    }

    @EventListener
    public void onPacketSendPost(PacketEvent.SendPost event) {
        if (event.getPacket() instanceof CPacketEntityAction) {
            switch (((CPacketEntityAction)event.getPacket()).getAction()) {
                case START_SPRINTING: {
                    this.sprinting = true;
                    break;
                }
                case STOP_SPRINTING: {
                    this.sprinting = false;
                    break;
                }
                case START_SNEAKING: {
                    this.sneaking = true;
                    break;
                }
                case STOP_SNEAKING: {
                    this.sneaking = false;
                    break;
                }
            }
        }

        if (event.getPacket() instanceof CPacketClickWindow) {
            this.clickTimer.reset();
            if (!this.windowClicks && this.isStrict()) {
                this.release();
            }
        }
    }

    @EventListener
    public void onWorldClientLoad(WorldClientEvent.Load event) {
        this.endedSneak = false;
        this.endedSprint = false;
        this.windowClicks = false;
    }

    @EventListener
    public void onUpdatePost(TickEvent event) {
        ticks++;
    }

    public TimerUtil getClickTimer() {
        return this.clickTimer;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public void setStrict(boolean strict) {
        if (this.strict && !strict) {
            this.releaseMultiClick();
        }
        this.strict = strict;
    }

    public void startMultiClick() {
        this.windowClicks = true;
    }

    public void releaseMultiClick() {
        this.windowClicks = false;
        this.release();
    }


    public boolean passedInTicks(int tickPassed) {
        if (tickPassed < ticks) {
            ticks = 0;
            return true;
        } else return false;
    }

    public boolean passed(int ms) {
        int delayedTicks = ms / 10;
        if (delayedTicks < ticks && System.currentTimeMillis() - this.lagTimer.get() >= (long) ms) {
            ticks = 0;
            reset();
            return true;
        } else return false;
    }

    public long getTimeStamp() {
        return this.lagTimer.get();
    }

    public void reset() {
        this.lagTimer.set(System.currentTimeMillis());
    }

    private void release() {
        if (this.endedSneak) {
            this.endedSneak = false;
            mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (this.endedSprint) {
            this.endedSprint = false;
            mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }
}
