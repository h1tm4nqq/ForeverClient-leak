package we.devs.forever.client.modules.impl.misc.autoeat;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.util.EnumHand;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

/**
 * @author SrRina
 * @since 20/02/2021 at 00:25
 **/
public class AutoEat extends Module {
    public static Setting<Mode> settingMode = new Setting<>("Mode", Mode.Health, "Modes to verify if eats or no!");

    /* Food misc. */
    public static Setting<Integer> settingFood = new Setting<Integer>("Food", 10, 1, 20, "Food stats to start eat!", v -> settingMode.getValue() == Mode.Food);

    /* Health misc. */
    public static Setting<Boolean> settingOnlyGoldenApple = new Setting("OnlyGApple", false, "Only golden eat!", v -> settingMode.getValue() == Mode.Health);
    public static Setting<Integer> settingHealth = new Setting<Integer>("Health", 20, 1, 36, "Health to start eat.", v -> settingMode.getValue() == Mode.Health);

    private boolean isToEat;
    private boolean isReturned;

    private int oldSlot;
    private int newSlot;

    public AutoEat() {
        super("AutoEat", "Find an food in hot bar if is hunger and automatically eat.", Category.MISC);
    }

//    @Override
//    public void onSetting() {
//        settingFood.setEnabled(settingMode.getValue() == Mode.FOOD);
//        settingFoodFill.setEnabled(settingMode.getValue() == Mode.FOOD);
//
//        settingHealth.setEnabled(settingMode.getValue() == Mode.HEALTH);
//        settingHealthFill.setEnabled(settingMode.getValue() == Mode.HEALTH);
//        settingOnlyGoldenApple.setEnabled(settingMode.getValue() == Mode.HEALTH);
//
//        if (settingHealthFill.getValue().intValue() <= settingHealth.getValue().intValue()) {
//            settingHealthFill.setValue(settingHealth.getValue());
//        }
//
//        if (settingFoodFill.getValue().intValue() <= settingFood.getValue().intValue()) {
//            settingFoodFill.setValue(settingFood.getValue());
//        }
//    }
    

    @Override
    public void onTick() {
        if (fullNullCheck()) {
            return;
        }

        if (mc.player.isCreative()) {
            return;
        }

        if (this.isToEat) {
            boolean flagO = this.newSlot == 40; // Offhand.
            boolean flagM = this.newSlot != 40; // Main hand.

            if (settingMode.getValue() == Mode.Health && !settingOnlyGoldenApple.getValue() && mc.player.getFoodStats().getFoodLevel() == 20) {
                this.isToEat = false;
            }

            // Set to back old slot.
            this.isReturned = true;

            /*
             * Offhand doesn't have food but main hand yes!!
             */
            if (!flagO) {
                Item item = mc.player.inventory.getStackInSlot(this.newSlot).getItem();

                if (this.doAccept(item)) {
                    mc.player.inventory.currentItem = this.newSlot;

                    this.doEat();
                } else {
                    // Last check to disable.
                    this.newSlot = this.findFoodSlot();
                    this.isToEat = this.newSlot == -1;
                }
            }

            /*
             * Main hand doesn't have food but offhand yes.
             */
            if (!flagM) {
                Item item = mc.player.inventory.getStackInSlot(this.newSlot).getItem();

                if (this.doAccept(item)) {
                    this.doEat();
                } else {
                    // Last check to disable.
                    this.newSlot = this.findFoodSlot();
                    this.isToEat = this.newSlot == -1;
                }
            }
        } else {
            if (this.isReturned) {
                mc.gameSettings.keyBindUseItem.pressed = false;
                mc.player.inventory.currentItem = this.oldSlot;

                this.isReturned = false;
            } else {
                this.oldSlot = mc.player.inventory.currentItem;
            }
        }

        int slot = this.findFoodSlot();

        if (slot != -1) {
            Item item = mc.player.inventory.getStackInSlot(slot).getItem();

            switch ((Mode) settingMode.getValue()) {
                case Health: {
                    this.isToEat = mc.player.getHealth() <= settingHealth.getValue().intValue() && (mc.player.getFoodStats().getFoodLevel() != 20 || settingOnlyGoldenApple.getValue());

                    break;
                }

                case Food: {
                    this.isToEat = mc.player.getFoodStats().getFoodLevel() <= settingFood.getValue().intValue();

                    break;
                }
            }
        }
    }

    /**
     * Make player eat.
     */
    public void doEat() {
        // We need verify hand.
        EnumHand hand = this.doAccept(mc.player.getHeldItemOffhand().getItem()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        mc.gameSettings.keyBindUseItem.pressed = true;

        // Process right click.
        mc.playerController.processRightClick(mc.player, mc.world, hand);
    }

    public int findFoodSlot() {
        int slot = -1;

        if (this.doAccept(mc.player.getHeldItemOffhand().getItem())) {
            return 40; // 40 is offhand slot!
        }

        for (int i = 0; i < 9; i++) {
            final Item items = mc.player.inventory.getStackInSlot(i).getItem();

            if (doAccept(items)) {
                slot = i;

                break;
            }
        }

        return slot;
    }

    public boolean doAccept(Item item) {
        boolean isAccepted = false;

        if (item == Items.GOLDEN_APPLE && settingOnlyGoldenApple.getValue() && settingOnlyGoldenApple.getValue()) {
            isAccepted = true;
        } else if (item instanceof ItemFood && (!settingOnlyGoldenApple.getValue())) {
            if (item != Items.CHORUS_FRUIT || item != Items.ROTTEN_FLESH || item != Items.POISONOUS_POTATO || item != Items.SPIDER_EYE) {
                isAccepted = true;
            }
        }

        return isAccepted;
    }
}
