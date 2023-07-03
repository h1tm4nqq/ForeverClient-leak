package we.devs.forever.client.modules.impl.combat;

import net.minecraft.block.BlockShulkerBox;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.HashSet;
import java.util.Set;

public class AntiRegear extends Module {
    private final Set<Object> shulkerBlackList = new HashSet<>();
    public Setting<Float> targetRange = (new Setting<>("Target Range", 10.0F, 0.0F, 20.0F, "If player went in specified range shulker will be broken"));
    public Setting<Float> radius = (new Setting<>("Radius", 4.0F, 0.0F, 6.0F, "Radius in where client will break shulkers"));
    public Setting<Boolean> rotate = (new Setting<>("Rotate", true, "Rotates your camera to the shulker that you are breaking"));

    public AntiRegear() {
        super("AntiRegear", "Mines regear shulkers", Category.COMBAT);
    }


    @EventListener
    public void onWalkingPlayerUpdate(MotionEvent.Pre event) {
        if (DamageUtil.getTarget(this.targetRange.getValue()) != null) {
            if (mc.player.onGround) {

                for (BlockPos pos : BlockUtil.getSphere(this.radius.getValue(), true)) {
                    if (Util.mc.world.getBlockState(pos).getBlock() instanceof BlockShulkerBox && !this.shulkerBlackList.contains(pos)) {

                        int lastSlot = mc.player.inventory.currentItem;
                        int pickSlot = InventoryUtil.getItemFromHotbar(Items.DIAMOND_PICKAXE);
                        InventoryUtil.switchSilent(pickSlot, pickSlot, lastSlot, AutoSwitch.Silent);
                        if(rotate.getValue()) {
                            rotationManager.doRotation(RotationType.Normal, pos,10);

                        }
                        mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                        mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));

                        InventoryUtil.switchSilent(lastSlot, pickSlot, lastSlot, AutoSwitch.Silent);
                        Util.mc.player.swingArm(EnumHand.MAIN_HAND);

                    }
                }
            }
        }
    }



    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        CPacketPlayerTryUseItemOnBlock packet;
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && Util.mc.player.getHeldItem((packet = event.getPacket()).getHand()).getItem() instanceof ItemShulkerBox) {
            this.shulkerBlackList.add(packet.getPos().offset(packet.getDirection()));
        }
    }
}
