//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.CPacketClickWindow
 *  net.minecraft.network.play.client.CPacketEntityAction
 *  net.minecraft.network.play.client.CPacketEntityAction$Action
 *  net.minecraft.network.play.server.SPacketPlayerPosLook
 */
package we.devs.forever.api.manager.impl.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.network.WorldClientEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.TimerUtil;

import java.util.concurrent.atomic.AtomicLong;

public class NCPManager
        extends AbstractManager
        implements Util {
    private final AtomicLong lagTimer = new AtomicLong();
    private final TimerUtil clickTimer = new TimerUtil();
    private boolean endedSprint;
    private boolean endedSneak;
    private boolean windowClicks;
    private boolean strict;
    private volatile boolean sneaking;
    private volatile boolean sprinting;
    public int ticks = 0;

    public NCPManager() {
        super("Ncp manager");
    }

    @EventListener(priority = ListenerPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            this.lagTimer.set(System.currentTimeMillis());
        }
    }

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
            switch (((CPacketEntityAction) event.getPacket()).getAction()) {
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
    public void onUpdatePost(MotionEvent.Post event) {
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

    public boolean passed(int ms) {
        return System.currentTimeMillis() - this.lagTimer.get() >= (long) ms;
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
            mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (this.endedSprint) {
            this.endedSprint = false;
            mc.player.connection.sendPacket((Packet) new CPacketEntityAction((Entity) mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}

