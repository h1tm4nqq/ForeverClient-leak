package we.devs.forever.client.modules.impl.player;

import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.client.PairUtil;
import we.devs.forever.api.util.combat.CombatUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.HashMap;
import java.util.Map;

public class Refill
        extends Module {
    public final Setting<Boolean> strict = (new Setting<>("Strict", false, "Strict setting for strict anticheat like 2bpvp"));
    public final Setting<Integer> threshold = (new Setting<>("Threshold", 10, 1, 63));
    public final Setting<Integer> tickDelay = (new Setting<>("Tick Delay", 0, 0, 10));
    public int delayStep = 0;

    public Refill() {
        super("Refill", "Automatically refills items in your hotbar", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if ((Refill.mc.currentScreen instanceof GuiContainer)) {
            return;
        }
        if (this.delayStep < this.tickDelay.getValue()) {
            ++this.delayStep;
            return;
        }
        this.delayStep = 0;

//        if (strictCheck()) return;

        PairUtil<Integer, Integer> slots = this.findReplenishableHotbarSlot();
        if (slots == null) {
            return;
        }
        int inventorySlot = slots.getKey();
        int hotbarSlot = slots.getValue();
        if (!strict.getValue()) {
            Refill.mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, Refill.mc.player);
            Refill.mc.playerController.windowClick(0, hotbarSlot, 0, ClickType.PICKUP, Refill.mc.player);
            Refill.mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, Refill.mc.player);
            Refill.mc.playerController.updateController();
        } else mc.playerController.windowClick(0, inventorySlot, 0, ClickType.QUICK_MOVE, mc.player);
    }

    private PairUtil<Integer, Integer> findReplenishableHotbarSlot() {
        PairUtil<Integer, Integer> returnPair = null;
        for (Map.Entry<Integer, ItemStack> hotbarSlot : this.getHotbar().entrySet()) {
            int inventorySlot;
            ItemStack stack = hotbarSlot.getValue();
            if (stack.isEmpty
                    || stack.getItem() == Items.AIR
                    || !stack.isStackable()
                    || stack.stackSize >= stack.getMaxStackSize()
                    || (stack.stackSize > this.threshold.getValue() && stack.getMaxStackSize() != 1)
                    || (inventorySlot = this.findCompatibleInventorySlot(stack)) == -1)
                continue;
            returnPair = new PairUtil<Integer, Integer>(inventorySlot, hotbarSlot.getKey());
        }
        return returnPair;
    }

    private int findCompatibleInventorySlot(ItemStack hotbarStack) {
        int inventorySlot = -1;
        int smallestStackSize = 999;
        for (Map.Entry<Integer, ItemStack> entry : this.getInventory().entrySet()) {
            int currentStackSize;
            ItemStack inventoryStack = entry.getValue();
            if (inventoryStack.isEmpty || inventoryStack.getItem() == Items.AIR || !this.isCompatibleStacks(hotbarStack, inventoryStack) || smallestStackSize <= (currentStackSize = Refill.mc.player.inventoryContainer.getInventory().get(entry.getKey().intValue()).stackSize))
                continue;
            smallestStackSize = currentStackSize;
            inventorySlot = entry.getKey();
        }
        return inventorySlot;
    }

    private boolean isCompatibleStacks(ItemStack stack1, ItemStack stack2) {
        if (!stack1.getItem().equals(stack2.getItem())) {
            return false;
        }
        if (stack1.getItem() instanceof ItemBlock && stack2.getItem() instanceof ItemBlock) {
            Block block1 = ((ItemBlock) stack1.getItem()).getBlock();
            Block block2 = ((ItemBlock) stack2.getItem()).getBlock();
            if (!block1.blockState.equals(block2.blockState)) {
                return false;
            }
        }
        return stack1.getDisplayName().equals(stack2.getDisplayName()) && stack1.getItemDamage() == stack2.getItemDamage();
    }

    private boolean strictCheck() {
        return strict.getValue() && !CombatUtil.isInHole(mc.player) /*&& mc.player.getDistanceSq(EntityUtil.getClosestEnemy(10)) > 6*/ && EntityUtil.isMoving() && BlockUtil.isBlockSolid(mc.player.getPosition());
    }

    private Map<Integer, ItemStack> getInventory() {
        return this.getInvSlots(9, 35);
    }

    private Map<Integer, ItemStack> getHotbar() {
        return this.getInvSlots(36, 44);
    }

    private Map<Integer, ItemStack> getInvSlots(int current, int last) {
        HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, Refill.mc.player.inventoryContainer.getInventory().get(current));
            ++current;
        }
        return fullInventorySlots;
    }
}
