package we.devs.forever.client.modules.impl.player;

import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import static we.devs.forever.client.modules.impl.exploit.PearlPhase.Method2822;

public
class MiddleClickPearl extends Module {

    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.MiddleClick));
    private final Setting<Boolean> stopRotation = (new Setting<>("Rotation", true));
    private final Setting<Boolean> cooldownBypass = (new Setting<>("CoolDownBypass", true));
    private final Setting<Integer> rotation = (new Setting<>("Delay", 10, 0, 100, v -> stopRotation.getValue()));

    private final TimerUtil timer = new TimerUtil();
    private boolean clicked = false;

    public MiddleClickPearl() {
        super("MiddleClickPearl", "Throws a pearl", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck() && mode.getValue() == Mode.Toggle) {
            throwPearl();
            this.disable();
        }
    }

    @Override
    public void onTick() {//MotionEvent event) {
        if (/*event.getStage() == 0 && */mode.getValue() == Mode.MiddleClick) {
            if (Mouse.isButtonDown(2)) {
                if (!clicked) {
                    throwPearl();
                }
                clicked = true;
            } else {
                clicked = false;
            }
        }
    }

    @EventListener(priority = ListenerPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        //This prevents other modules from changing the rotationYaw after us
        if (stopRotation.getValue() && !timer.passedMs(rotation.getValue()) && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();
            packet.yaw = mc.player.rotationYaw;
            packet.pitch = mc.player.rotationPitch;
        }
    }

    private void throwPearl() {
        int pearlSlot = InventoryUtil.findHotbarBlock(ItemEnderPearl.class);
        boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
     if(cooldownBypass.getValue())   Method2822(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ));
        if (pearlSlot != -1 || offhand) {
            int oldslot = mc.player.inventory.currentItem;
            if (!offhand) {
                InventoryUtil.switchToHotbarSlot(pearlSlot, false);
            }
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            if (!offhand) {
                InventoryUtil.switchToHotbarSlot(oldslot, false);
            }
            //timer.reset();
        }
    }

    public
    enum Mode {
        Toggle,
        MiddleClick
    }
}
