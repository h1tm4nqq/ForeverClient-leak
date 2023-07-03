package we.devs.forever.client.modules.impl.combat.offhand;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.server.SPacketEntityStatus;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerPacketReceive extends ModuleListener<OffHand, PacketEvent.Receive> {
    public ListenerPacketReceive(OffHand module) {
        super(module, PacketEvent.Receive.class);
    }

    @Override
    public void invoke(PacketEvent.Receive event) {
        if (nullCheck()) return;

            // packet for totem pops
            if (event.getPacket() instanceof SPacketEntityStatus && ((SPacketEntityStatus) event.getPacket()).getOpCode() == 35) {

                // entity that popped
                Entity entity = ((SPacketEntityStatus) event.getPacket()).getEntity(mc.world);

                // player has popped
                if (entity.equals(mc.player)) {

                    // item slot
                    // find our item in our inventory
                    int itemSlot = -1;
                    for (int i = 9; i < (module.hotbar.getValue() ? 45 : 36); i++) {
                        if (mc.player.inventoryContainer.getSlot(i).getStack().getItem().equals(Items.TOTEM_OF_UNDYING)) {
                            itemSlot = i;
                            break;
                        }
                    }

                    // found our item
                    if (itemSlot != -1) {

                        // switch to items in one cycle
                        if (module.fast.getValue()) {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((module.speed.getMax() - module.speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayed = module.speed.getValue() >= module.speed.getMax() || module.offhandTimer.passedMs(offhandDelay);

                            // passed delay
                            if (delayed) {

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // move the item to the offhand
                                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                // if we didn't get any item to swap
                                if (mc.player.inventory.getItemStack().isEmpty()) {

                                    // reset
                                    module.offhandTimer.reset();
                                    return;
                                }

                                // find a slot to return to
                                int returnSlot = -1;
                                for (int i = 0; i < 36; i++) {
                                    if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                        returnSlot = i;
                                        break;
                                    }
                                }

                                // move the item in the offhand to the return slot
                                if (returnSlot != -1) {
                                    mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                                    mc.playerController.updateController();
                                }

                                // reset
                                module. offhandTimer.reset();
                            }
                        }

                        // switch to item in multiple cycles
                        else {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((module.speed.getMax() -module.speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayedFirst =module.speed.getValue() >=module.speed.getMax() || module.offhandTimer.passedMs(offhandDelay);

                            // passed delay
                            if (delayedFirst) {

                                // stop active hand prevents failing
                                mc.player.stopActiveHand();

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // we have waited the proper time ???
                                boolean delayedSecond =module.speed.getValue() >=module.speed.getMax() ||module. offhandTimer.passedMs(offhandDelay * 2);

                                // passed delay
                                if (delayedSecond) {

                                    // stop active hand prevents failing
                                    mc.player.stopActiveHand();

                                    // move the item to the offhand
                                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                    // if we didn't get any item to swap
                                    if (mc.player.inventory.getItemStack().isEmpty()) {

                                        // reset
                                        module.offhandTimer.reset();
                                        return;
                                    }

                                    // we have waited the proper time ???
                                    boolean delayedThird =module.speed.getValue() >=module.speed.getMax() || module.offhandTimer.passedMs(offhandDelay * 3);

                                    // passed delay
                                    if (delayedThird) {

                                        // find a slot to return to
                                        int returnSlot = -1;
                                        for (int i = 0; i < 36; i++) {
                                            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                                returnSlot = i;
                                                break;
                                            }
                                        }

                                        // move the item in the offhand to the return slot
                                        if (returnSlot != -1) {

                                            // stop active hand prevents failing
                                            mc.player.stopActiveHand();

                                            // click
                                            mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
                                            mc.playerController.updateController();
                                        }

                                        // reset
                                        module. offhandTimer.reset();
                                    }
                                }
                            }
                        }
                    }
                
            }
        }
    }
}
