package we.devs.forever.api.util.player.inventory;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.Client;
import we.devs.forever.mixin.mixins.accessor.IPlayerControllerMP;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public
class InventoryUtil implements Util {

    public static boolean isHolding(Item item) {
        return mc.player.getHeldItemMainhand().getItem().equals(item) || mc.player.getHeldItemOffhand().getItem().equals(item);
    }
    public static boolean isHolding(Class<? extends Item> clazz) {
        return clazz.isInstance(mc.player.getHeldItemMainhand().getItem()) || clazz.isInstance(mc.player.getHeldItemOffhand().getItem());
    }
    public static void switchSilent(int slot, int slotbb, int oldSlot, AutoSwitch silent) {
        if (slot < 0) {
            return;
        }
        switch (silent) {
            case Normal:
                Client.inventoryManager.moveInventory(() -> {
                    setCurrentItem(slot);
                });
                Client.inventoryManager.finish();
                break;
            case Silent:
//                    mc.player.inventory.currentItem = slot;
                Client.inventoryManager.moveInventory(() -> {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot)); //test
                    syncItem();
                });
                Client.inventoryManager.finish();
                break;
            case SilentSlotPacket:
                Client.inventoryManager.moveInventory(() -> {
                    // transaction id
                    short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                    // window click
                    ItemStack itemstack = mc.player.openContainer.slotClick(
                            hotbarToInventory(slotbb), oldSlot, ClickType.SWAP, mc.player);

                    mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, hotbarToInventory(slotbb),
                            oldSlot, ClickType.SWAP, itemstack, nextTransactionID));
                    PlayerUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
                    syncItem();
                });
                Client.inventoryManager.finish();
                break;
            case SilentSlot:
                Client.inventoryManager.moveInventory(() -> {
                    mc.playerController.windowClick(0, InventoryUtil.hotbarToInventory(slotbb), oldSlot, ClickType.SWAP, mc.player);
                    PlayerUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
                    syncItem();
                });
                Client.inventoryManager.finish();
                break;
        }
    }

    public static void setCurrentItem(int slot) {
        mc.player.inventory.currentItem = slot;
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        mc.playerController.updateController();
    }

    public static void switchToSlot(int swapSlot, AutoSwitch swap) {

        // check if the slot is actually in the hotbar
        if (InventoryPlayer.isHotbar(swapSlot)) {

            // not already at the correct slot
            if (mc.player.inventory.currentItem != swapSlot) {
                switch (swap) {
//                    case NORMAL:
//                        // update our current item and send a packet to the server
//                        mc.player.inventory.currentItem = in;
//                        mc.player.connection.sendPacket(new CPacketHeldItemChange(in));
//                        break;
                    case Silent:
                        // send a switch packet to the server, should be silent client-side
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(swapSlot));
                        ((IPlayerControllerMP) mc.playerController).setCurrentPlayerItem(swapSlot);
//                         ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
                        break;
                    case SilentSlot:
                        // transaction id
                        short nextTransactionID = mc.player.openContainer.getNextTransactionID(mc.player.inventory);

                        // window click
                        ItemStack itemstack = mc.player.openContainer.slotClick(swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
                        mc.player.connection.sendPacket(new CPacketClickWindow(mc.player.inventoryContainer.windowId, swapSlot, mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));

                        // confirm packets
                        mc.player.connection.sendPacket(new CPacketConfirmTransaction(mc.player.inventoryContainer.windowId, nextTransactionID, true));
                        break;
                }
            }
        }
    }

    public static void syncItem() {
        ((IPlayerControllerMP) mc.playerController).syncItem();
    }


    public static int hotbarToInventory(int slot) {
        if (slot == -2) {
            return 45;
        }

        if (slot > -1 && slot < 9) {
            return 36 + slot;
        }

        return slot;
    }

    public static void switchToHotbarSlot(int slot, boolean silent) {
        if (mc.player.inventory.currentItem == slot || slot < 0) {
            return;
        }

        if (silent) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            mc.playerController.updateController();
        } else {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
        }
    }


    public static int findSlotHotbar(Class clazz) {
        for (int i = 0; i < 9; ++i) {
            ItemStack item = InventoryUtil.mc.player.inventory.getStackInSlot(i);

            if (item != ItemStack.EMPTY) {
                if (clazz.isInstance(item.getItem())) {
                    return i;
                }

                if (item.getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock) item.getItem()).getBlock();

                    if (clazz.isInstance(block)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    public static void switchToHotbarSlot(Class clazz, boolean silent) {
        int slot = findHotbarBlock(clazz);
        if (slot > -1) {
            switchToHotbarSlot(slot, silent);
        }
    }

    public static boolean isNull(ItemStack stack) {
        return stack == null || stack.getItem() instanceof ItemAir;
    }

    public static int findHotbarBlock(Class clazz) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY) {
                continue;
            }

            if (clazz.isInstance(stack.getItem())) {
                return i;
            }

            if (stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (clazz.isInstance(block)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean heldItem(Item item, Hand hand) {
        switch (hand) {
            case Main: {
                if (InventoryUtil.mc.player.getHeldItemMainhand().getItem() != item) break;
                return true;
            }
            case Off: {
                if (InventoryUtil.mc.player.getHeldItemOffhand().getItem() != item) break;
                return true;
            }
            case Both: {
                if (InventoryUtil.mc.player.getHeldItemOffhand().getItem() != item && InventoryUtil.mc.player.getHeldItemMainhand().getItem() != item)
                    break;
                return true;
            }
        }
        return false;
    }

    public static int findHotbarBlock(Block blockIn) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY) {
                continue;
            }

            if (stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block == blockIn) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int getItemHotbar(Item input) {
        for (int i = 0; i < 9; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (Item.getIdFromItem(item) == Item.getIdFromItem(input)) {
                return i;
            }
        }
        return -1;
    }

    public static int findStackInventory(Item input) {
        return findStackInventory(input, false);
    }

    public static int findStackInventory(Item input, boolean withHotbar) {
        for (int i = withHotbar ? 0 : 9; i < 36; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (Item.getIdFromItem(input) == Item.getIdFromItem(item)) {
                return i + (i < 9 ? 36 : 0);
            }
        }
        return -1;
    }
    public static int findItemInventorySlot(Predicate<ItemStack> item, boolean offHand, boolean withXCarry) {
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (item.test(entry.getValue())) {
                if (entry.getKey() == 45 && !offHand) {
                    continue;
                }
                return entry.getKey();
            }
        }
        if (withXCarry) {
            for (int i = 1; i < 5; i++) {
                 if(item.test(mc.player.inventoryContainer.inventorySlots.get(i).getStack())) return i;

            }
        }
        return -1;
    }
    public static int findItemInventorySlot(Item item, boolean offHand) {
        int slot = -1;
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() == item) {
                if (entry.getKey() == 45 && !offHand) {
                    continue;
                }
                slot = entry.getKey();
                return slot;
            }
        }
        return -1;
    }

    public static int findInventorySlotByClass(Class<?> item, boolean offHand) {
        int slot = -1;
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getClass() == item) {
                if (entry.getKey() == 45 && !offHand) {
                    continue;
                }
                slot = entry.getKey();
            }
        }
        return slot;
    }


    public static List<Integer> findEmptySlots(boolean withXCarry) {
        List<Integer> outPut = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().isEmpty || entry.getValue().getItem() == Items.AIR) {
                outPut.add(entry.getKey());
            }
        }

        if (withXCarry) {
            for (int i = 1; i < 5; i++) {
                Slot craftingSlot = mc.player.inventoryContainer.inventorySlots.get(i);
                ItemStack craftingStack = craftingSlot.getStack();
                if (craftingStack.isEmpty() || craftingStack.getItem() == Items.AIR) {
                    outPut.add(i);
                }
            }
        }
        return outPut;
    }


    public static int findInventoryBlock(Class<?> clazz, boolean offHand) {
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (isBlock(entry.getValue().getItem(), clazz)) {
                if (entry.getKey() == 45 && !offHand) {
                    continue;
                }
                slot.set(entry.getKey());
                return slot.get();
            }
        }
        return slot.get();
    }

    public static boolean isBlock(Item item, Class<?> clazz) {
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            return clazz.isInstance(block);
        }
        return false;
    }

    public static void confirmSlot(int slot) {
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        mc.player.inventory.currentItem = slot;
        mc.playerController.updateController();
    }

    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        return getInventorySlots(9, 44);
    }

    private static Map<Integer, ItemStack> getInventorySlots(int currentI, int last) {
        int current = currentI;
        Map<Integer, ItemStack> fullInventorySlots = new HashMap<>();
        while (current <= last) {
            fullInventorySlots.put(current, mc.player.inventoryContainer.getInventory().get(current));
            current++;
        }
        return fullInventorySlots;
    }

    public static boolean[] switchItem(boolean back, int lastHotbarSlot, boolean switchedItem, Switch mode, Class clazz) {
        boolean[] switchedItemSwitched = {switchedItem, false};
        switch (mode) {
            case Normal:
                if (!back && !switchedItem) {
                    switchToHotbarSlot(findHotbarBlock(clazz), false);
                    switchedItemSwitched[0] = true;
                } else if (back && switchedItem) {
                    switchToHotbarSlot(lastHotbarSlot, false);
                    switchedItemSwitched[0] = false;
                }
                switchedItemSwitched[1] = true;
                break;
            case Silent:
                if (!back && !switchedItem) {
                    switchToHotbarSlot(findHotbarBlock(clazz), true);
                    switchedItemSwitched[0] = true;
                } else if (back && switchedItem) {
                    switchedItemSwitched[0] = false;
                    //++  ForeverClient.inventoryManager.recoverSilent(lastHotbarSlot);
                }
                switchedItemSwitched[1] = true;
                break;
            case None:
                if (back) {
                    switchedItemSwitched[1] = true;
                } else {
                    switchedItemSwitched[1] = mc.player.inventory.currentItem == findHotbarBlock(clazz);
                }
        }
        return switchedItemSwitched;
    }


    public static boolean holdingItem(Class clazz) {
        boolean result = false;
        ItemStack stack = mc.player.getHeldItemMainhand();
        result = isInstanceOf(stack, clazz);
        if (!result) {
            ItemStack offhand = mc.player.getHeldItemOffhand();
            result = isInstanceOf(stack, clazz);
        }

        return result;
    }

    public static boolean isInstanceOf(ItemStack stack, Class clazz) {
        if (stack == null) {
            return false;
        }

        Item item = stack.getItem();
        if (clazz.isInstance(item)) {
            return true;
        }

        if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            return clazz.isInstance(block);
        }

        return false;
    }

    public static int getEmptyXCarry() {
        for (int i = 1; i < 5; i++) {
            Slot craftingSlot = mc.player.inventoryContainer.inventorySlots.get(i);
            ItemStack craftingStack = craftingSlot.getStack();
            if (craftingStack.isEmpty() || craftingStack.getItem() == Items.AIR) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isSlotEmpty(int i) {
        Slot slot = mc.player.inventoryContainer.inventorySlots.get(i);
        ItemStack stack = slot.getStack();
        return stack.isEmpty();
    }

    public static int convertHotbarToInv(int input) {
        return 45 - 9 + input;
    }

    public static boolean areStacksCompatible(ItemStack stack1, ItemStack stack2) {

        if (!stack1.getItem().equals(stack2.getItem())) {
            return false;
        }

        if ((stack1.getItem() instanceof ItemBlock) && (stack2.getItem() instanceof ItemBlock)) {
            Block block1 = ((ItemBlock) stack1.getItem()).getBlock();
            Block block2 = ((ItemBlock) stack2.getItem()).getBlock();
            if (!block1.material.equals(block2.material)) {
                return false;
            }
        }

        if (!stack1.getDisplayName().equals(stack2.getDisplayName())) {
            return false;
        }

        return stack1.getItemDamage() == stack2.getItemDamage();
    }

    public static EntityEquipmentSlot getEquipmentFromSlot(int slot) {
        if (slot == 5) {
            return EntityEquipmentSlot.HEAD;
        } else if (slot == 6) {
            return EntityEquipmentSlot.CHEST;
        } else if (slot == 7) {
            return EntityEquipmentSlot.LEGS;
        } else {
            return EntityEquipmentSlot.FEET;
        }
    }

    public static int findArmorSlot(EntityEquipmentSlot type, boolean binding) {
        int slot = -1;
        float damage = 0;
        for (int i = 9; i < 45; i++) {
            final ItemStack s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).getStack();
            if (s.getItem() != Items.AIR && s.getItem() instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) s.getItem();
                if (armor.armorType == type) {
                    final float currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s));
                    final boolean cursed = binding && (EnchantmentHelper.hasBindingCurse(s));
                    if (currentDamage > damage && !cursed) {
                        damage = currentDamage;
                        slot = i;
                    }
                }
            }
        }
        return slot;
    }

    public static int findArmorSlot(EntityEquipmentSlot type, boolean binding, boolean withXCarry) {
        int slot = findArmorSlot(type, binding);
        if (slot == -1 && withXCarry) {
            float damage = 0;
            for (int i = 1; i < 5; i++) {
                Slot craftingSlot = mc.player.inventoryContainer.inventorySlots.get(i);
                ItemStack craftingStack = craftingSlot.getStack();
                if (craftingStack.getItem() != Items.AIR && craftingStack.getItem() instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor) craftingStack.getItem();
                    if (armor.armorType == type) {
                        final float currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, craftingStack));
                        final boolean cursed = binding && (EnchantmentHelper.hasBindingCurse(craftingStack));
                        if (currentDamage > damage && !cursed) {
                            damage = currentDamage;
                            slot = i;
                        }
                    }
                }
            }
        }
        return slot;
    }








    public static int findBlockSlotInventory(Class clazz, boolean offHand, boolean withXCarry) {
        int slot = findInventoryBlock(clazz, offHand);
        if (slot == -1 && withXCarry) {
            for (int i = 1; i < 5; i++) {
                Slot craftingSlot = mc.player.inventoryContainer.inventorySlots.get(i);
                ItemStack craftingStack = craftingSlot.getStack();
                if (craftingStack.getItem() != Items.AIR) {
                    Item craftingStackItem = craftingStack.getItem();
                    if (clazz.isInstance(craftingStackItem)) {
                        slot = i;
                    } else if (craftingStackItem instanceof ItemBlock) {
                        Block block = ((ItemBlock) craftingStackItem).getBlock();
                        if (clazz.isInstance(block)) {
                            slot = i;
                        }
                    }
                }
            }
        }
        return slot;
    }

    public static int findAnyBlock() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) continue;
            return i;
        }
        return -1;
    }

    public static int findAnyBlock0() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY
                    || !(stack.getItem() instanceof ItemBlock)
                    || BlockUtil.isBlockUnSolid(((ItemBlock) stack.getItem()).block)) continue;
            return i;
        }
        return -1;
    }

    public static int findAnyBlock(Block[] ignore) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtil.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY
                    || !(stack.getItem() instanceof ItemBlock)
                    || Arrays.stream(ignore).anyMatch(b -> b == ((ItemBlock) stack.getItem()).block)
                    || BlockUtil.isBlockUnSolid(((ItemBlock) stack.getItem()).block)) continue;
            return i;
        }
        return -1;
    }

    public static List<Integer> getItemInventory(Item item) {
        List<Integer> ints = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            Item target = mc.player.inventory.getStackInSlot(i).getItem();

            if (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(item)) ints.add(i);
        }

        if (ints.size() == 0) ints.add(-1);

        return ints;
    }

    public static int getItemFromHotbar(Item item) {
        int slot = -1;

        for (int i = 0; i < 9; ++i) {
            if (Util.mc.player.inventory.getStackInSlot(i).getItem() == item) {
                slot = i;
                break;
            }
        }

        return slot;
    }

    public static void packetSwap(final int slot) {
        if (slot != -1) {
            InventoryUtil.mc.getConnection().sendPacket(new CPacketHeldItemChange(slot));
        }
    }

    public static void switchToSlot(int slot) {
        if (slot != -1 && mc.player.inventory.currentItem != slot) {
            mc.player.inventory.currentItem = slot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        }
    }

    public static int find(Item item) {
        int b = -1;
        for (int a = 0; a < 9; a++) {
            if (mc.player.inventory.getStackInSlot(a).getItem() == item) {
                b = a;
            }
        }
        return b;
    }

    public static boolean switchTo(Item item) {
        int a = find(item);
        if (a == -1) return false;
        mc.player.inventory.currentItem = a;
        mc.playerController.updateController();
        return true;
    }

    public
    enum Switch {
        Normal,
        Silent,
        None
    }

    public enum Hand {
        Main,
        Off,
        Both

    }

    public static
    class Task {

        private final int slot;
        private final boolean update;
        private final boolean quickClick;

        public Task() {
            this.update = true;
            this.slot = -1;
            this.quickClick = false;
        }

        public Task(int slot) {
            this.slot = slot;
            this.quickClick = false;
            this.update = false;
        }

        public Task(int slot, boolean quickClick) {
            this.slot = slot;
            this.quickClick = quickClick;
            this.update = false;
        }

        public void run() {
            if (this.update) {
                mc.playerController.updateController();
            }

            if (slot != -1) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, this.slot, 0, this.quickClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, mc.player);
            }
        }

        public boolean isSwitching() {
            return !this.update;
        }
    }
}