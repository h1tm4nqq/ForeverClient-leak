package we.devs.forever.client.modules.impl.combat.offhand;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import we.devs.forever.api.event.events.client.TickEvent;
import we.devs.forever.api.util.combat.CrystalUtils;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.listener.ModuleListener;

import java.util.Arrays;
import java.util.List;


public class ListenerTick extends ModuleListener<OffHand, TickEvent> {
    public ListenerTick(OffHand module) {
        super(module, TickEvent.class);
    }

    @Override
    public void invoke(TickEvent event) {

        // can't switch while we are in a screen
        if (mc.currentScreen == null) {

            // item we are switching to
            Item item =module.mode.getValue().getItem();

            // check if offhand should be overridden
            if (module.offhandOverride.getValue()) {

                // holding a sword and interacting
                if (InventoryUtil.isHolding(ItemSword.class) && mc.gameSettings.keyBindUseItem.isKeyDown()) {

                    // block we are interacting with
                    Block interactBlock = mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();

                    // check if it gets activated or if it is a button/lever
                    if (!shiftBlocks.contains(interactBlock) && !interactBlock.equals(Blocks.STONE_BUTTON) && !interactBlock.equals(Blocks.WOODEN_BUTTON) && !interactBlock.equals(Blocks.LEVER)) {
                        item = Items.GOLDEN_APPLE;
                    }
                }
            }

            // make sure we can actually take damage
            if (DamageUtil.canTakeDamage()) {

                // player health
                double playerHealth = PlayerUtil.getHealth();

                // check lethal scenarios
                if (module.lethal.getValue()) {

                    // SCENARIO #1: fall damage
                    float fallDamage = ((mc.player.fallDistance - 3) / 2F) + 3.5F;

                    // fall damage will kill us
                    if (playerHealth - fallDamage < 0.5 && !mc.player.isOverWater()) {
                        item = Items.TOTEM_OF_UNDYING;
                    }

                    // SCENARIO #2: flight damage
                    if (mc.player.isElytraFlying() || mc.player.capabilities.isFlying) {
                        item = Items.TOTEM_OF_UNDYING;
                    }

                    // SCENARIO #3: crystal damage
                    for (Entity entity : mc.world.loadedEntityList) {

                        // make sure the entity exists
                        if (entity == null || entity.isDead) {
                            continue;
                        }

                        // make sure crystal is in range
                        double crystalRange = mc.player.getDistance(entity);
                        if (crystalRange > 6) {
                            continue;
                        }

                        if (entity instanceof EntityEnderCrystal) {

                            // damage from crystal
                            double crystalDamage =   CrystalUtils.calculateDamage(entity, mc.player);

                            // crystal will kill us
                            if (playerHealth - crystalDamage < 0.5) {
                                item = Items.TOTEM_OF_UNDYING;
                                break;
                            }
                        }
                    }
                }

                // make sure we are not below our critical health
                if (playerHealth <=module. health.getValue()) {
                    item = Items.TOTEM_OF_UNDYING;
                }

                // item slot
                // find our item in our inventory
                int itemSlot = -1;

                // gapple slots
                int gappleSlot = -1;
                int crappleSlot = -1;

                // search inventory
                for (int i = 9; i < (module.hotbar.getValue() ? 45 : 36); i++) {

                    // check item
                    if (mc.player.inventoryContainer.getSlot(i).getStack().getItem().equals(item)) {

                        // golden apple
                        if (item.equals(Items.GOLDEN_APPLE)) {

                            // item stack
                            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();

                            // god apple
                            if (stack.hasEffect()) {
                                gappleSlot = i;
                            }

                            // crapple
                            else {
                                crappleSlot = i;
                            }
                        }

                        else {
                            itemSlot = i;
                            break;
                        }
                    }
                }

                // since there are two types of gapples we need to sort them
                if (item.equals(Items.GOLDEN_APPLE)) {

                    // use crapples
                    if (module.crapple.getValue()) {

                        // player has absorption hearts
                        if (mc.player.isPotionActive(MobEffects.ABSORPTION)) {

                            // use a crapple
                            // in 1.12.2 this will restore all of our absorption hearts
                            if (crappleSlot != -1) {
                                itemSlot = crappleSlot;
                            }

                            // if we don't have crapples then use gapples
                            else if (gappleSlot != -1) {
                                itemSlot = gappleSlot;
                            }
                        }

                        // if we don't have absorption hearts then the crapple won't restore us back to full absorption hearts
                        else if (gappleSlot != -1) {
                            itemSlot = gappleSlot;
                        }
                    }

                    // don't use crapples
                    else {

                        // prefer gapples
                        if (gappleSlot != -1) {
                            itemSlot = gappleSlot;
                        }

                        // fall back to crapples
                        else if (crappleSlot != -1) {
                            itemSlot = crappleSlot;
                        }
                    }
                }

                // found our item
                if (itemSlot != -1) {

                    // already in offhand
                    if (!module.isOffhand(mc.player.inventoryContainer.getSlot(itemSlot).getStack())) {

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
                                mc.playerController.windowClick(0, itemSlot, 0, ClickType.PICKUP, mc.player);

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
                                module.offhandTimer.reset();
                            }
                        }

                        // switch to item in multiple cycles
                        else {

                            // calculate if we have passed delays
                            // offhand delay based on offhand speeds
                            long offhandDelay = (long) ((module.speed.getMax() - module.speed.getValue()) * 50);

                            // we have waited the proper time ???
                            boolean delayedFirst = module.speed.getValue() >= module.speed.getMax() || module.offhandTimer.passedMs(offhandDelay);

                            // passed delay
                            if (delayedFirst) {

                                // stop active hand prevents failing
                                mc.player.stopActiveHand();

                                // pickup
                                mc.playerController.windowClick(0, itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, ClickType.PICKUP, mc.player);

                                // we have waited the proper time ???
                                boolean delayedSecond = module.speed.getValue() >=module. speed.getMax() || module.offhandTimer.passedMs(offhandDelay * 2);

                                // passed delay
                                if (delayedSecond) {

                                    // stop active hand prevents failing
                                    mc.player.stopActiveHand();

                                    // move the item to the offhand
                                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

                                    // if we didn't get any item to swap
                                    if (mc.player.inventory.getItemStack().isEmpty()) {

                                        // reset
                                        module. offhandTimer.reset();
                                        return;
                                    }

                                    // we have waited the proper time ???
                                    boolean delayedThird = module.speed.getValue() >=module. speed.getMax() || module.offhandTimer.passedMs(offhandDelay * 3);

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
                                        module.   offhandTimer.reset();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private static final List<Block> shiftBlocks = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL, // :troll:
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR,
            Blocks.ENCHANTING_TABLE,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
            // Blocks.COMMAND_BLOCK,
            // Blocks.CHAIN_COMMAND_BLOCK
    );
}
