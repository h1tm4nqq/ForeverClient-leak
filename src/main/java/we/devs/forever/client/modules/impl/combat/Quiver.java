package we.devs.forever.client.modules.impl.combat;


import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.player.StopUsingItemEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.player.XCarry;
import we.devs.forever.client.setting.Setting;

public class Quiver extends Module {

    public static final Setting<Boolean> speed = new Setting<>("Swiftness", false);
    public static final Setting<Boolean> strength = new Setting<>("Strength", false);
    public static final Setting<Integer> ticks = new Setting<>("Ticks", 5, 1, 15);
    public static final Setting<Boolean> toggelable = new Setting<>("Toggelable", false);
    public static final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", false);
    public static final Setting<Boolean> rearrange = new Setting<>("Rearrange", false);
    public static final Setting<Boolean> noGapSwitch = new Setting<>("NoGapSwitch", false);

    private TimerUtil timer = new TimerUtil();

    private boolean cancelStopUsingItem = false;

    public Quiver() {
        super("Quiver", "Shoots yourself", Category.COMBAT);
    }

    @EventListener
    public void onUpdateWalkingPlayer(MotionEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (!timer.passedMs(3000)) return;

        if (noGapSwitch.getValue() && mc.player.getActiveItemStack().getItem() instanceof ItemFood) return;

        if (strength.getValue() && !mc.player.isPotionActive(MobEffects.STRENGTH)) {
            if (isFirstAmmoValid("Arrow of Strength")) {
                shootBow(event);
            } else if (toggelable.getValue()) {
                toggle();
            }
        }

        if (speed.getValue() && !mc.player.isPotionActive(MobEffects.SPEED)) {
            if (isFirstAmmoValid("Arrow of Swiftness")) {
                shootBow(event);
            } else if (toggelable.getValue()) {
                toggle();
            }
        }
    }

    @EventListener
    public void onStopUsingItem(StopUsingItemEvent event) {
        if (cancelStopUsingItem) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onEnable() {
        cancelStopUsingItem = false;
    }

    private void shootBow(MotionEvent.Pre event) {
        if (mc.player.inventory.getCurrentItem().getItem() == Items.BOW) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, -90, mc.player.onGround));
            rotationManager.doRotation(RotationType.Legit, 0F, -90F, 10);
            if (mc.player.getItemInUseMaxCount() >= 5) {
                cancelStopUsingItem = false;
                mc.playerController.onStoppedUsingItem(mc.player);
                if (toggelable.getValue()) {
                    toggle();
                }
                timer.reset();
            } else if (mc.player.getItemInUseMaxCount() == 0) {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                cancelStopUsingItem = true;
            }
        } else if (autoSwitch.getValue()) {
            int bowSlot = getBowSlot();
            if (bowSlot != -1 && bowSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = bowSlot;
                mc.playerController.updateController();
            }
        }
    }

    public int getBowSlot() {
        int bowSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.BOW) {
            bowSlot = Module.mc.player.inventory.currentItem;
        }


        if (bowSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.BOW) {
                    bowSlot = l;
                    break;
                }
            }
        }

        return bowSlot;
    }

    private boolean isFirstAmmoValid(String type) {
     int slot = InventoryUtil.findItemInventorySlot(item -> item.getDisplayName().equalsIgnoreCase(type), true, moduleManager.getModuleByClass(XCarry.class).isEnabled());
        if (slot == -1) {
            return true;
        } else if (rearrange.getValue()) {
            return rearrangeArrow(slot, type);
        } else {
            return false;
        }
    }

    private boolean rearrangeArrow(int fakeSlot, String type) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TIPPED_ARROW) {
                if (itemStack.getDisplayName().equalsIgnoreCase(type)) {
                    mc.playerController.windowClick(0, fakeSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, fakeSlot, 0, ClickType.PICKUP, mc.player);
                    return true;
                }
            }
        }
        return false;
    }
}
