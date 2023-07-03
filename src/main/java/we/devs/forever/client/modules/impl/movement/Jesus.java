package we.devs.forever.client.modules.impl.movement;


import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.util.math.AxisAlignedBB;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.JesusEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.player.Freecam;
import we.devs.forever.client.setting.Setting;

public class Jesus extends Module {
    public static AxisAlignedBB offset;
    private static Jesus INSTANCE;

    static {
        Jesus.offset = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9999, 1.0);
        Jesus.INSTANCE = new Jesus();
    }

    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Normal));
    public Setting<Boolean> cancelVehicle = (new Setting<>("NoVehicle", false));
    public Setting<EventMode> eventMode = (new Setting<>("Jump", EventMode.Pre, v -> this.mode.getValue() == Mode.Trampoline));
    public Setting<Boolean> fall = (new Setting<>("NoFall", false, v -> this.mode.getValue() == Mode.Trampoline));
    private boolean grounded;

    public Jesus() {
        super("Jesus", "Allows you to walk on water like the legend.", Category.MOVEMENT);
        Jesus.INSTANCE = this;
    }

    public static Jesus getInstance() {
        if (Jesus.INSTANCE == null) {
            Jesus.INSTANCE = new Jesus();
        }
        return Jesus.INSTANCE;
    }

    @EventListener
    public void onUpdateWalkingPlayerPre(MotionEvent.Pre event) {
        if (fullNullCheck() || Freecam.INSTANCE.isEnabled()) {
            return;
        }
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0 && (this.mode.getValue() == Mode.Bounce || this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.Normal) && !Jesus.mc.player.isSneaking() && !Jesus.mc.player.noClip && !Jesus.mc.gameSettings.keyBindJump.isKeyDown() && EntityUtil.isInLiquid()) {
            Jesus.mc.player.motionY = 0.10000000149011612;
        }
        if (event.getStage() == 0 && this.mode.getValue() == Mode.Trampoline && (this.eventMode.getValue() == EventMode.All || this.eventMode.getValue() == EventMode.Pre)) {
            this.doTrampoline();
        } else if (event.getStage() == 1 && this.mode.getValue() == Mode.Trampoline && (this.eventMode.getValue() == EventMode.All || this.eventMode.getValue() == EventMode.Post)) {
            this.doTrampoline();
        }
    }

    @EventListener
    public void sendPacket(final PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && Freecam.INSTANCE.isDisabled() && (this.mode.getValue() == Mode.Bounce || this.mode.getValue() == Mode.Normal) && Jesus.mc.player.getRidingEntity() == null && !Jesus.mc.gameSettings.keyBindJump.isKeyDown()) {
            final CPacketPlayer packet = event.getPacket();
            if (!EntityUtil.isInLiquid() && EntityUtil.isOnLiquid(0.05000000074505806) && EntityUtil.checkCollide() && Jesus.mc.player.ticksExisted % 3 == 0) {
                final CPacketPlayer cPacketPlayer = packet;
                cPacketPlayer.y -= 0.05000000074505806;
            }
        }
    }

    @EventListener
    public void onLiquidCollision(final JesusEvent event) {
        if (fullNullCheck() || Freecam.INSTANCE.isEnabled()) {
            return;
        }
        if (event.getStage() == 0 && (this.mode.getValue() == Mode.Bounce || this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.Normal) && Jesus.mc.world != null && Jesus.mc.player != null && EntityUtil.checkCollide() && Jesus.mc.player.motionY < 0.10000000149011612 && event.getPos().getY() < Jesus.mc.player.posY - 0.05000000074505806) {
            if (Jesus.mc.player.getRidingEntity() != null) {
                event.setBoundingBox(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.949999988079071, 1.0));
            } else {
                event.setBoundingBox(Block.FULL_BLOCK_AABB);
            }
            event.cancel();
        }
    }

    @EventListener
    public void onPacketReceived(final PacketEvent.Receive event) {
        if (this.cancelVehicle.getValue() && event.getPacket() instanceof SPacketMoveVehicle) {
            event.cancel();
        }
    }

    public String getDisplayInfo() {
        if (this.mode.getValue() == Mode.Normal) {
            return null;
        }
        return this.mode.currentEnumName();
    }

    private void doTrampoline() {
        if (Jesus.mc.player.isSneaking()) {
            return;
        }
        if (EntityUtil.isAboveLiquid(Jesus.mc.player) && !Jesus.mc.player.isSneaking() && !Jesus.mc.gameSettings.keyBindJump.pressed) {
            Jesus.mc.player.motionY = 0.1;
            return;
        }
        if (Jesus.mc.player.onGround || Jesus.mc.player.isOnLadder()) {
            this.grounded = false;
        }
        if (Jesus.mc.player.motionY > 0.0) {
            if (Jesus.mc.player.motionY < 0.03 && this.grounded) {
                final EntityPlayerSP player = Jesus.mc.player;
                player.motionY += 0.06713;
            } else if (Jesus.mc.player.motionY <= 0.05 && this.grounded) {
                final EntityPlayerSP player2 = Jesus.mc.player;
                player2.motionY *= 1.20000000999;
                final EntityPlayerSP player3 = Jesus.mc.player;
                player3.motionY += 0.06;
            } else if (Jesus.mc.player.motionY <= 0.08 && this.grounded) {
                final EntityPlayerSP player4 = Jesus.mc.player;
                player4.motionY *= 1.20000003;
                final EntityPlayerSP player5 = Jesus.mc.player;
                player5.motionY += 0.055;
            } else if (Jesus.mc.player.motionY <= 0.112 && this.grounded) {
                final EntityPlayerSP player6 = Jesus.mc.player;
                player6.motionY += 0.0535;
            } else if (this.grounded) {
                final EntityPlayerSP player7 = Jesus.mc.player;
                player7.motionY *= 1.000000000002;
                final EntityPlayerSP player8 = Jesus.mc.player;
                player8.motionY += 0.0517;
            }
        }
        if (this.grounded && Jesus.mc.player.motionY < 0.0 && Jesus.mc.player.motionY > -0.3) {
            final EntityPlayerSP player9 = Jesus.mc.player;
            player9.motionY += 0.045835;
        }
        if (!this.fall.getValue()) {
            Jesus.mc.player.fallDistance = 0.0f;
        }
        if (!EntityUtil.checkForLiquid(Jesus.mc.player, true)) {
            return;
        }
        if (EntityUtil.checkForLiquid(Jesus.mc.player, true)) {
            Jesus.mc.player.motionY = 0.5;
        }
        this.grounded = true;
    }

    public enum Mode {
        Trampoline,
        Bounce,
        Vanilla,
        Normal
    }

    public enum EventMode {
        Pre,
        Post,
        All
    }
}
