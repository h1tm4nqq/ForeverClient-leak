package we.devs.forever.client.modules.impl.misc;

import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.player.ProcessRightClickBlockEvent;
import we.devs.forever.api.event.events.world.BlockEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.alts.tools.Pair;

public class AntiAim extends Module {
    public Setting<Boolean> lag = new Setting<>("Lag", true);
    public Setting<Float> lagSpeed = new Setting<>("LagSpeed", 1F, 0F, 10F, v -> lag.getValue());
    public Setting<Integer> maxYaw = new Setting<>("MaxYaw", 90, 1, 180, v -> lag.getValue());
    public Setting<Integer> yaw = new Setting<>("Yaw", 0, -180, 180, v -> !lag.getValue());
    public Setting<Integer> pitch = new Setting<>("Pitch", 0, -90, 90, v -> !lag.getValue());
    public Setting<Boolean> blocks = new Setting<>("RotateToBlocks", true);
    Pair<Float, Float> old = new Pair<>(90F, 0F);
    boolean side = false;

    public AntiAim() {
        super("AntiAim", "Aim your rotates", Category.MISC);
    }

    public static final TimerUtil timerUtil = new TimerUtil();

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem cPacketPlayerTryUseItem = event.getPacket();
            ItemStack handStack = mc.player.getHeldItem(cPacketPlayerTryUseItem.getHand());
            if (!handStack.isEmpty() && handStack.getItem() instanceof ItemEnderPearl) {
                rotationManager.doRotation(RotationType.Packet, rotationManager.getYaw(), rotationManager.getPitch(), 16);
                timerUtil.reset();
            }
        }

    }

    @EventListener
    public void onRots(MotionEvent.Pre event) {
        if (timerUtil.passedMs(500L)) {
            if (!lag.getValue()) {
                event.setRotations(yaw.getValue(), pitch.getValue());
            } else {
                old = getAngle(old);
                event.setRotations(old.getFirst(), old.getSecond());
            }

        }


    }

    @EventListener
    public void onRightClick(ProcessRightClickBlockEvent event) {
        if (blocks.getValue()) {
            rotationManager.doRotation(RotationType.Packet, event.pos, 16);
            timerUtil.reset();
        }
    }

    @EventListener
    public void onLeftClick(BlockEvent event) {
        if (blocks.getValue()) {
            rotationManager.doRotation(RotationType.Packet, event.pos, 16);
            timerUtil.reset();
        }
    }

    private Pair<Float, Float> getAngle(Pair<Float, Float> old) {
        if (old == null) {
            return new Pair<>(0F, 0F);
        }
        if (old.getFirst() < -maxYaw.getValue()) {
            side = true;
            old.setFirst((float) -maxYaw.getValue());
            return old;
        }
        if (old.getFirst() > maxYaw.getValue()) {
            side = false;
            old.setFirst(Float.valueOf(maxYaw.getValue()));
            return old;
        }
        if (side) {
            old.setFirst(check(old.getFirst() + 15F * lagSpeed.getValue()));
        } else {
            old.setFirst(check(old.getFirst() - 15F * lagSpeed.getValue()));
        }
        old.setSecond(Math.abs(checkPitch(old.getFirst())));
        return old;

    }

    float check(float f) {
        if (f < -maxYaw.getValue()) {
            side = true;
            return -maxYaw.getValue();
        }
        if (f > maxYaw.getValue()) {
            side = false;
            return maxYaw.getValue();
        }
        return f;
    }

    float checkPitch(float f) {
        if (f < -90) {
            side = true;
            return -90;
        }
        if (f > 90) {
            side = false;
            return 90;
        }
        return f;
    }
}
