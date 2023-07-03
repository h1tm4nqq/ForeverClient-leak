package we.devs.forever.client.modules.impl.combat;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public
class AutoArmor extends Module {

    private final Setting<Integer> delay = (new Setting<>("Delay", 50, 0, 500));
    private final Setting<Boolean> mendingTakeOff = (new Setting<>("AutoMend", false));
    private final Setting<Integer> closestEnemy = (new Setting<>("Enemy", 8, 1, 20, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> helmetThreshold = (new Setting<>("Helmet%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> chestThreshold = (new Setting<>("Chest%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> legThreshold = (new Setting<>("Legs%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Integer> bootsThreshold = (new Setting<>("Boots%", 80, 1, 100, v -> mendingTakeOff.getValue()));
    private final Setting<Boolean> curse = (new Setting<>("CurseOfBinding", false));
    private final Setting<Integer> actions = (new Setting<>("Actions", 3, 1, 12));
    private final Setting<Bind> elytraBind = (new Setting<>("Elytra", new Bind(-1)));
    private final Setting<Boolean> notify = (new Setting<>("Notify",true));
    private final Setting<Boolean> tps = (new Setting<>("TpsSync", true));
    private final Setting<Boolean> updateController = (new Setting<>("Update", true));
    private final Setting<Boolean> shiftClick = (new Setting<>("ShiftClick", false));

    private final TimerUtil timerUtil = new TimerUtil();
    private final TimerUtil elytraTimerUtil = new TimerUtil();
    private final Queue<InventoryUtil.Task> taskList = new ConcurrentLinkedQueue<>();
    private final List<Integer> doneSlots = new ArrayList<>();
    private boolean elytraOn = false;
    private volatile boolean throwExp = false;

    public AutoArmor() {
        super("AutoArmor", "Puts Armor on for you.", Category.COMBAT);
    }

    @EventListener
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState() && !(mc.currentScreen instanceof ForeverClientGui) && elytraBind.getValue().getKey() == Keyboard.getEventKey()) {
            elytraOn = !elytraOn;
          if(notify.getValue()) sendMessage(elytraOn ? "Switch ChestPlate to Elytra" : "Switch Elytra to ChestPlate");
        }
    }

    @Override
    public void onLogin() {
        timerUtil.reset();
        elytraTimerUtil.reset();
    }

    @Override
    public void onDisable() {
        taskList.clear();
        doneSlots.clear();
        elytraOn = false;
    }

    @Override
    public void onLogout() {
        taskList.clear();
        doneSlots.clear();
    }
    @EventListener
    public void onPacketSend(PacketEvent.SendPost event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
            CPacketPlayerTryUseItem packet2 = event.getPacket();
            if(mc.player.getHeldItem(packet2.getHand()).getItem().equals(Items.EXPERIENCE_BOTTLE)) throwExp = true;

        }
    }
    @Override
    public void onTick() {
        if (fullNullCheck() || (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory))) {
            return;
        }

        if (taskList.isEmpty()) {
            if (mendingTakeOff.getValue() && (isSafe() || EntityUtil.isSafe(AutoArmor.mc.player, 1, false, true))) {
                if (throwExp) {
                    throwExp =false;
                    final ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();
                    if (!helm.isEmpty) {
                        int helmDamage = DamageUtil.getRoundedDamage(helm);
                        if (helmDamage >= helmetThreshold.getValue()) {
                            takeOffSlot(5);
                        }
                    }

                    final ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();
                    if (!chest.isEmpty) {
                        int chestDamage = DamageUtil.getRoundedDamage(chest);
                        if (chestDamage >= chestThreshold.getValue()) {
                            takeOffSlot(6);
                        }
                    }

                    final ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();
                    if (!legging.isEmpty) {
                        int leggingDamage = DamageUtil.getRoundedDamage(legging);
                        if (leggingDamage >= legThreshold.getValue()) {
                            takeOffSlot(7);
                        }
                    }

                    final ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();
                    if (!feet.isEmpty) {
                        int bootDamage = DamageUtil.getRoundedDamage(feet);
                        if (bootDamage >= bootsThreshold.getValue()) {
                            takeOffSlot(8);
                        }
                    }
                    return;
                }
            }

            final ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();
            if (helm.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, curse.getValue());
                if (slot != -1) {
                    getSlotOn(5, slot);
                }
            }

            final ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();
            if (chest.getItem() == Items.AIR) {
                if (taskList.isEmpty()) {
                    if (elytraOn && elytraTimerUtil.passedMs(500)) {
                        int elytraSlot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false);
                        if (elytraSlot != -1) {
                            if ((elytraSlot < 5 && elytraSlot > 1) || !shiftClick.getValue()) {
                                taskList.add(new InventoryUtil.Task(elytraSlot));
                                taskList.add(new InventoryUtil.Task(6));
                            } else {
                                taskList.add(new InventoryUtil.Task(elytraSlot, true));
                            }

                            if (updateController.getValue()) {
                                taskList.add(new InventoryUtil.Task());
                            }
                            elytraTimerUtil.reset();
                        }
                    } else if (!elytraOn) {
                        final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, curse.getValue());
                        if (slot != -1) {
                            getSlotOn(6, slot);
                        }
                    }
                }
            } else {
                if (elytraOn && chest.getItem() != Items.ELYTRA && elytraTimerUtil.passedMs(500)) {
                    if (taskList.isEmpty()) {
                        final int slot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false);
                        if (slot != -1) {
                            taskList.add(new InventoryUtil.Task(slot));
                            taskList.add(new InventoryUtil.Task(6));
                            taskList.add(new InventoryUtil.Task(slot));
                            if (updateController.getValue()) {
                                taskList.add(new InventoryUtil.Task());
                            }
                        }
                        elytraTimerUtil.reset();
                    }
                } else if (!elytraOn && chest.getItem() == Items.ELYTRA && elytraTimerUtil.passedMs(500) && taskList.isEmpty()) {
                    //TODO: WTF IS THIS
                    int slot = InventoryUtil.findItemInventorySlot(Items.DIAMOND_CHESTPLATE, false);
                    if (slot == -1) {
                        slot = InventoryUtil.findItemInventorySlot(Items.IRON_CHESTPLATE, false);
                        if (slot == -1) {
                            slot = InventoryUtil.findItemInventorySlot(Items.GOLDEN_CHESTPLATE, false);
                            if (slot == -1) {
                                slot = InventoryUtil.findItemInventorySlot(Items.CHAINMAIL_CHESTPLATE, false);
                                if (slot == -1) {
                                    slot = InventoryUtil.findItemInventorySlot(Items.LEATHER_CHESTPLATE, false);
                                }
                            }
                        }
                    }

                    if (slot != -1) {
                        taskList.add(new InventoryUtil.Task(slot));
                        taskList.add(new InventoryUtil.Task(6));
                        taskList.add(new InventoryUtil.Task(slot));
                        if (updateController.getValue()) {
                            taskList.add(new InventoryUtil.Task());
                        }
                    }
                    elytraTimerUtil.reset();
                }
            }

            final ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();
            if (legging.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, curse.getValue());
                if (slot != -1) {
                    getSlotOn(7, slot);
                }
            }

            final ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();
            if (feet.getItem() == Items.AIR) {
                final int slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, curse.getValue());
                if (slot != -1) {
                    getSlotOn(8, slot);
                }
            }
        }

        if (timerUtil.passedMs((int) (delay.getValue() * (tps.getValue() ? serverManager.getTpsFactor() : 1)))) {
            if (!taskList.isEmpty()) {
                for (int i = 0; i < actions.getValue(); i++) {
                    InventoryUtil.Task task = taskList.poll();
                    if (task != null) {
                        task.run();
                    }
                }
            }
            timerUtil.reset();
        }
    }

    @Override
    public String getDisplayInfo() {
        if (elytraOn) {
            return "Elytra";
        } else {
            return null;
        }
    }

    private void takeOffSlot(int slot) {
        if (taskList.isEmpty()) {
            int target = -1;
            for (int i : InventoryUtil.findEmptySlots(false)) {
                if (!doneSlots.contains(target)) {
                    target = i;
                    doneSlots.add(i);
                }
            }

            if (target != -1) {
                if ((target < 5 && target > 0) || !shiftClick.getValue()) {
                    taskList.add(new InventoryUtil.Task(slot));
                    taskList.add(new InventoryUtil.Task(target));
                } else {
                    taskList.add(new InventoryUtil.Task(slot, true));
                }
                if (updateController.getValue()) {
                    taskList.add(new InventoryUtil.Task());
                }
            }
        }
    }

    private void getSlotOn(int slot, int target) {
        if (taskList.isEmpty()) {
            doneSlots.remove((Object) target);
            if ((target < 5 && target > 0) || !shiftClick.getValue()) {
                taskList.add(new InventoryUtil.Task(target));
                taskList.add(new InventoryUtil.Task(slot));
            } else {
                taskList.add(new InventoryUtil.Task(target, true));
            }
            if (updateController.getValue()) {
                taskList.add(new InventoryUtil.Task());
            }
        }
    }

    /*private static class Task {
        private final int slot;
        private boolean update = false;
        private boolean quickClick = false;
        public Task(int slot, boolean quickClick) {
            this.slot = slot;
            this.quickClick = quickClick;
        }
        public Task(int slot) {
            this.slot = slot;
            this.quickClick = false;
        }
        public Task() {
            this.update = true;
            this.slot = -1;
        }
        public void run() {
            if(this.update) {
                mc.playerController.updateController();
            }
            if(slot != -1) {
                mc.playerController.windowClick(0, this.slot, 0, this.quickClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, mc.player);
            }
        }
        public boolean isSwitching() {
            return !this.update;
        }
    }*/

    private boolean isSafe() {
        EntityPlayer closest = EntityUtil.getClosestEnemy(closestEnemy.getValue());
        if (closest == null) {
            return true;
        }
        return mc.player.getDistanceSq(closest) >= MathUtil.square(closestEnemy.getValue());
    }

}