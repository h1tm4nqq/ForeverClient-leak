package we.devs.forever.client.modules.impl.combat.offhandold;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.CrystalUtils;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.hole.TwoHole;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.player.inventory.Task;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.player.Refill;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Offhand
        extends Module {
    private static Offhand instance;
    private final Queue<Task> taskList;
    final TimerUtil timer;
    final TimerUtil delay0 = new TimerUtil();
    final TimerUtil secondTimer;
    final TimerUtil thirdtimer;
    public Mode currentMode;
    public int switchval;
    public int totems;
    public int crystals;
    public int gapples;
    public int lastTotemSlot;
    public int lastGappleSlot;
    public int lastCrystalSlot;
    public int lastObbySlot;
    public int lastWebSlot;
    public boolean holdingCrystal;
    public boolean holdingTotem;
    public boolean holdingGapple;
    public boolean didSwitchThisTick;
    public Setting<Page> pageSetting = (new Setting<>("Page", Page.Main));
    public Setting<Mode> offhandmode = (new Setting<>("Offhand", Mode.Totems, "Mode", v -> pageSetting.getValue() == Page.Main));
    public Setting<Boolean> crystal = (new Setting<>("Crystal", false, "Put crystals to offhand if no totems", v -> pageSetting.getValue() == Page.Main && offhandmode.getValue().equals(Mode.Totems)));
    public Setting<RightClick> rightGap = (new Setting<>("Right Click Gap", RightClick.Always, v -> pageSetting.getValue() == Page.Main));
    public Setting<Boolean> fast = (new Setting<>("Fast", true, v -> pageSetting.getValue() == Page.Main));
    public Setting<Float> delay = (new Setting<>("Delay", 10F, 0F, 10F, v -> pageSetting.getValue() == Page.Main));
    public Setting<Boolean> switchmode = (new Setting<>("KeyMode", false, v -> pageSetting.getValue() == Page.Main));
    public Setting<Bind> SwitchBind = (new Setting<>("SwitchKey", new Bind(-1), v -> switchmode.getValue() && pageSetting.getValue() == Page.Main));
    public Setting<Float> switchHp = (new Setting<>("SwitchHP", 16.5f, 0.1f, 36.0f, v -> pageSetting.getValue() == Page.Main));
    public Setting<Float> holeHP = (new Setting<>("HoleHP", 8.0f, 0.1f, 36.0f, v -> pageSetting.getValue() == Page.Main));
    public Setting<Boolean> armorCheck = (new Setting<>("ArmorCheck", false, v -> pageSetting.getValue() == Page.Main));
    public Setting<Integer> actions = (new Setting<>("Packets", 4, 1, 4, v -> pageSetting.getValue() == Page.Main));
    public Setting<Boolean> crystalCheck = (new Setting<>("Crystal-Check", true, v -> pageSetting.getValue() == Page.Main));
    public Setting<Boolean> totemElytra = (new Setting<>("TotemElytra", false, v -> pageSetting.getValue() == Page.Misc));
    public Setting<Boolean> notfromhotbar = (new Setting<>("NoHotbar", false, v -> pageSetting.getValue() == Page.Misc));
    public Setting<Boolean> fallcheck = (new Setting<>("FallCheck", true, v -> pageSetting.getValue() == Page.Misc));
    public Setting<Integer> falldistance = (new Setting<>("FallDistance", 100, 1, 100, v -> fallcheck.getValue() && pageSetting.getValue() == Page.Misc));
    public Setting<Boolean> lagSwitch = (new Setting<>("Anti Lag", false, v -> pageSetting.getValue() == Page.Misc));
    boolean second;

    public Offhand() {
        super("Offhand", "Allows you to switch up your ", Category.COMBAT);
        addModuleListeners(new ListenerPacketReceive(this));
        addModuleListeners(new ListenerKey(this));
        addModuleListeners(new ListenerProcessRightClickBlockEvent(this));
        addModuleListeners(new ListenerPacketSend(this));
        addModuleListeners(new ListenerMotionPre(this));
        instance = this;
        taskList = new ConcurrentLinkedQueue<>();
        timer = new TimerUtil();
        secondTimer = new TimerUtil();
        thirdtimer = new TimerUtil();
        currentMode = Mode.Totems;
        switchval = 0;
        totems = 0;
        crystals = 0;
        gapples = 0;
        lastTotemSlot = -1;
        lastGappleSlot = -1;
        lastCrystalSlot = -1;
        lastObbySlot = -1;
        lastWebSlot = -1;
        holdingCrystal = false;
        holdingTotem = false;
        holdingGapple = false;
        didSwitchThisTick = false;
        second = false;
    }

    @Override
    public String getDisplayInfo() {
        if (fullNullCheck()) return null;
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            return "Crystal";
        }
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            return "Totem";
        }
        if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
            return "Gapple";
        }
        return null;
    }


    public void doSwitch() {
        if (check()) {
            if (rightGap.getValue() != RightClick.Off
                    &&
                    !(mc.currentScreen instanceof GuiChat
                            || mc.currentScreen instanceof ForeverClientGui
                            || mc.player.getHeldItemMainhand().getItem() instanceof ItemFood
                            || mc.player.getHeldItemMainhand().getItem() instanceof ItemBow
                            || mc.player.getHeldItemMainhand().getItem() instanceof ItemEnderPearl
                            || mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock
                            || offhandmode.getValue().equals(Mode.Gapples)
                    )
                    && mc.gameSettings.keyBindUseItem.isKeyDown()) {

                if (rightGap.getValue() == RightClick.Always) {
                    currentMode = Mode.Gapples;
                } else {
                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                        currentMode = Mode.Gapples;
                    }
                }
            } else {
                switch (offhandmode.getValue()) {
                    case Crystals: {
                        if (crystals > 0) {
                            currentMode = Mode.Crystals;
                        } else if (gapples > 0) {
                            currentMode = Mode.Gapples;
                        } else {
                            currentMode = Mode.Totems;
                        }
                        break;
                    }
                    case Gapples: {
                        if (gapples > 0) {
                            currentMode = Mode.Gapples;
                        } else if (crystals > 0) {
                            currentMode = Mode.Crystals;
                        } else {
                            currentMode = Mode.Totems;
                        }
                        break;
                    }
                    case Totems: {
                        if (totems > 0) {
                            currentMode = Mode.Totems;
                        } else if (crystals > 0 && crystal.getValue()) {
                            currentMode = Mode.Crystals;
                        } else {
                            currentMode = Mode.Gapples;
                        }
                        break;
                    }
                }


            }
        } else {
            currentMode = Mode.Totems;
        }
        if (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        switch (currentMode) {
            case Totems: {
                if (totems <= 0 || holdingTotem) break;
                lastTotemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, false);
                putItemInOffhand(lastTotemSlot);
                break;
            }
            case Gapples: {
                if (gapples <= 0 || holdingGapple) break;
                lastGappleSlot = InventoryUtil.findItemInventorySlot(Items.GOLDEN_APPLE, false);
                putItemInOffhand(lastGappleSlot);
                break;
            }
            case Crystals: {
                if (crystals <= 0 || holdingCrystal) break;
                lastCrystalSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, false);
                putItemInOffhand(lastCrystalSlot);
                break;
            }
        }
        for (int i = 0; i < actions.getValue(); ++i) {
            Task task = taskList.poll();
            if (task == null) continue;
            mc.player.stopActiveHand();
            moduleManager.getModuleByClass(Refill.class).delayStep =0;
            task.run();
            if (!task.isSwitching()) continue;
            didSwitchThisTick = true;
        }
        delay0.reset();
    }


    public void countItems() {
        holdingCrystal = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        holdingTotem = mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING;
        holdingGapple = mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
        totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (holdingTotem) {
            totems += mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        }
        crystals = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
        if (holdingCrystal) {
            crystals += mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
        }
        gapples = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
        if (holdingGapple) {
            gapples += mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
        }
    }

    @SuppressWarnings("all")
    public boolean check() {
        BlockPos pos = new BlockPos(mc.player);
        if (armorCheck.getValue()
                && (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.AIR
                || mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.AIR
                || mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.AIR
                || mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.AIR)) return false;

        if (mc.player.isAirBorne
                && totemElytra.getValue()
                && mc.player.isElytraFlying()
                && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) return false;

        if (mc.player.fallDistance > falldistance.getValue() && fallcheck.getValue()) return false;

        if (lagSwitch.getValue() && serverManager.isServerNotResponding()) return false;

        if (!calcCrystal()) return false;

        return BlockUtil.validObi(pos)
                || BlockUtil.validBedrock(pos)
                || TwoHole.validTwoBlockObiXZ(pos, true, false) != null
                || !BlockUtil.isPosEmpty(new BlockPos(mc.player.getPositionVector()))

                ? !(EntityUtil.getHealth(mc.player, true) <= holeHP.getValue())
                : !(EntityUtil.getHealth(mc.player, true) <= switchHp.getValue());
    }

    private void putItemInOffhand(int slotIn) {
        if (fast.getValue()) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotIn, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotIn, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        } else if (slotIn != -1 && taskList.isEmpty()) {
            taskList.add(new Task(slotIn, ClickType.PICKUP));
            taskList.add(new Task(45, ClickType.PICKUP));
            taskList.add(new Task(slotIn, ClickType.PICKUP));
            taskList.add(new Task());
        }
    }

    public void setMode(Mode mode) {
        currentMode = currentMode == mode ? Mode.Totems : mode;
    }

    public boolean calcCrystal() {
        if (!crystalCheck.getValue()) return true;
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityEnderCrystal)) continue;
            if (entity.isDead) continue;
            if (mc.player.getDistanceSq(entity.getPosition()) > 36.0) continue;
            if (CrystalUtils.calculateDamage(entity, mc.player) >= EntityUtil.getHealth(mc.player)) return false;


        }
        return true;
    }

    public enum Page {
        Main,
        Misc
    }

    public enum RightClick {
        Off,
        Always,
        SwordAxe
    }

    public enum Mode {
        Totems(Items.TOTEM_OF_UNDYING),
        Gapples(Items.GOLDEN_APPLE),
        Crystals(Items.END_CRYSTAL);

        private final Item item;

        Mode(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }
}