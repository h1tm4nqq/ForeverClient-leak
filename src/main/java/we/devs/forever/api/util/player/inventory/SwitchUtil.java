package we.devs.forever.api.util.player.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.client.Client;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.mixin.mixins.accessor.IPlayerControllerMP;

import java.util.function.IntSupplier;

@SuppressWarnings("all")
public class SwitchUtil extends Client {

    private final Setting<AutoSwitch> setting;
    private int oldSlot;
    private int slotBB;
    private boolean swithced;
    private ItemStack slotBBStack;
    EntityPlayerMP player;


    public SwitchUtil(Setting<AutoSwitch> setting) {
        this.setting = setting;
    }

    public boolean switchToInv(Item... items) {
        for (Item item : items)
            if (switchTo(InventoryUtil.find(item)))
                return true;

        if (setting.getValue().equals(AutoSwitch.SilentSlot) || setting.getValue().equals(AutoSwitch.SilentSlotPacket)) {
            for (Item item : items)
                if (switchTo(mc.player.inventory.currentItem, InventoryUtil.findItemInventorySlot(item, false), false))
                    return true;
        }
        return false;
    }

    public boolean check(Block... blocks) {
        if (blocks.length < 1) throw new IllegalArgumentException("Not specified block");

        for (Block Block : blocks) {
            int slot = InventoryUtil.findHotbarBlock(Block);
            if (slot != -1) return true;
        }
        return false;
    }

    public boolean check(Item... items) {
        if (items.length < 1) throw new IllegalArgumentException("Not specified item");
        for (Item item : items) {
            int slot = InventoryUtil.find(item);
            if (slot != -1) return true;
        }
        return false;
    }

    public boolean switchTo(Item... items) {
        if (items.length < 1) throw new IllegalArgumentException("Not specified item");
        for (Item item : items)
            if (switchTo(InventoryUtil.find(item)))
                return true;

        return false;
    }

    public boolean switchTo(Block... blocks) {
        if (blocks.length < 1) throw new IllegalArgumentException("Not specified block");
        for (Block b : blocks)
            if (switchTo(InventoryUtil.findHotbarBlock(b)))
                return true;

        return false;
    }

    public boolean switchTo(IntSupplier intSupplier) {
        if (intSupplier == null) throw new IllegalArgumentException("intSupplier can't be null");
        return switchTo(intSupplier.getAsInt());
    }

    public boolean switchTo(int slot) {
        swithced = true;
        if (slot < 0 || slot > 9) {
            swithced = false;
            return false;
        }
        return switchTo(slot, hotbarToInventory(slot), true);
    }

    private boolean switchTo(int slot, int slotBB, boolean check) {
        if (fullNullCheck()) return false;
        oldSlot = mc.player.inventory.currentItem;
        this.slotBB = slotBB;
//        this.player = getPlayer();
        if (oldSlot == slot || !check) {
            swithced = true;
            return true;
        }
        slotBBStack = mc.player.inventory.getStackInSlot(slot);
        inventoryManager.moveInventory(() -> {
            switch (setting.getValue()) {
                case Normal:
                    InventoryUtil.setCurrentItem(slot);
                case Silent:
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                    syncItem();
//                    mc.playerController.updateController();
//                    InventoryUtil.setCurrentItem(slot);
                break;
            case SilentSlotPacket:
                // transaction id
                short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                // window click
                ItemStack itemstack = mc.player.openContainer.slotClick(
                        hotbarToInventory(slotBB), oldSlot, ClickType.SWAP, mc.player);

                mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, hotbarToInventory(slotBB),
                        oldSlot, ClickType.SWAP, itemstack, nextTransactionID));
                syncItem();
//                    PlayerUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
                break;
            case SilentSlot: {
                mc.playerController.windowClick(0, slotBB, oldSlot, ClickType.SWAP, mc.player);
                syncItem();

                break;
            }
            case PickUp: {
                oldSlot = slot;
                mc.playerController.pickItem(slot);
//                    mc.player.inventory.pickItem(slot);
//
//                    mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
//                    syncItem();
//                    fixManager.disableFix();
                    break;
                }
            }
        });
        return swithced;
    }


    public void switchBack() {
        if(setting == null) throw new IllegalStateException("Setting cannot be a null");
        if(setting.getValue().equals(AutoSwitch.None)) return;
        if(setting.getValue().equals(AutoSwitch.Normal)) return;
        if(!swithced) return;
        if (fullNullCheck()) return;
        //if (!swithced) return;
        if (oldSlot < 0 || oldSlot > 9) {
            return;
        }
        inventoryManager.finish(() -> {
            switch (setting.getValue()) {
                case Silent:
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                    syncItem();
//                    mc.playerController.updateController();
//                    InventoryUtil.setCurrentItem(oldSlot);
                    break;
                case SilentSlotPacket:
                    // transaction id
                    short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                    // window click
                    ItemStack itemstack = mc.player.openContainer.slotClick(
                            slotBB, oldSlot, ClickType.SWAP, mc.player);

                    mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, hotbarToInventory(slotBB),
                            oldSlot, ClickType.SWAP, itemstack, nextTransactionID));
//                        PlayerUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
                    syncItem();
                    break;

                case SilentSlot: {
                    mc.playerController.windowClick(0, slotBB, oldSlot, ClickType.SWAP, mc.player);
                    syncItem();
//                        PlayerUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));

                    break;
                }
                case PickUp: {
                    mc.playerController.pickItem(oldSlot);
//                    syncItem();
//                    fixManager.disableFix();
                    break;
                }

            }
        });
    }

    public void switchBackInv() {
        //if (!swithced) return;
        switch (setting.getValue()) {
            case Silent:
//                    mc.player.inventory.currentItem = oldSlot;
//                    syncItem();
                mc.player.inventory.currentItem = oldSlot;
                syncItem();
                break;
            case SilentSlotPacket:
                // transaction id
                short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                // window click
                ItemStack itemstack = mc.player.openContainer.slotClick(
                        slotBB, oldSlot, ClickType.SWAP, mc.player);

                mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, hotbarToInventory(slotBB),
                        oldSlot, ClickType.SWAP, itemstack, nextTransactionID));
                syncItem();
                break;
            case SilentSlot: {
                mc.playerController.windowClick(0, slotBB, oldSlot, ClickType.SWAP, mc.player);
                syncItem();
                break;
            }
            case PickUp: {
                mc.playerController.pickItem(oldSlot);
                syncItem();
                break;
            }
        }

    }


    private void syncItem() {
        ((IPlayerControllerMP) mc.playerController).syncItem();
    }


    private int hotbarToInventory(int slot) {
        if (slot == -2) {
            return 45;
        }

        if (slot > -1 && slot < 9) {
            return 36 + slot;
        }

        return slot;
    }
}
