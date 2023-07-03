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
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.DamageUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

/**
 * @author linustouchtips
 * @since 11/20/2021
 */
public class OffHand extends Module {
    public static OffHand INSTANCE;

    public OffHand() {
        super("AutoTotem", "Switches items in the offhand to a totem when low on health", Category.COMBAT);
        INSTANCE = this;
    }

    // **************************** general ****************************

    public Setting<Mode> mode = new Setting<>("Mode", Mode.TOTEM, "Item to use when not at critical health");

    public Setting<Float> health = new Setting<>("Health", 16.0F, 0.0F, 20.0F, "Critical health to switch to a totem");

    public Setting<Float> speed = new Setting<>("Speed", 20.0F, 0.0F, 20.0F, "Speed when switching items");

    public Setting<Boolean> fast = new Setting<>("Fast", false, "Performs all actions in one cycle");

    public Setting<Boolean> lethal = new Setting<>("Lethal", true, "Takes damage sources into account when switching");

    public Setting<Boolean> hotbar = new Setting<>("Hotbar", false, "Allow hotbar items to be moved to the offhand");

    public Setting<Boolean> crapple = new Setting<>("Crapple", false, "Uses a crapple in the offhand");

    public Setting<Boolean> offhandOverride = new Setting<>("OffhandOverride", true, "Switches offhand items in non-lethal scenarios");

    // offhand delay
    final TimerUtil offhandTimer = new TimerUtil();


    /**
     * Checks if a given item is already in the offhand
     *
     * @param in The given item
     * @return Whether a given item is already in the offhand
     */
    public boolean isOffhand(ItemStack in) {

        // item in the offhand
        ItemStack offhandItem = mc.player.getHeldItemOffhand();

        // two types of gapples so we need to check each one
        if (in.getItem().equals(Items.GOLDEN_APPLE)) {

            // holding golden apple
            if (offhandItem.getItem().equals(in.getItem())) {

                // given item is a gapple ?
                boolean gapple = in.hasEffect();

                // check if equal
                return gapple == offhandItem.hasEffect();
            }
        }

        // check if they are equal
        else {
            return offhandItem.getItem().equals(in.getItem());
        }

        return false;
    }

    public enum Mode {

        /**
         * Switch to an End Crystal
         */
        CRYSTAL(Items.END_CRYSTAL),

        /**
         * Switch to a Golden Apple
         */
        GAPPLE(Items.GOLDEN_APPLE),

        /**
         * Switch to a Totem
         */
        TOTEM(Items.TOTEM_OF_UNDYING);

        private final Item item;

        Mode(Item item) {
            this.item = item;
        }

        /**
         * Gets the item associated with the offhand
         *
         * @return The item associated with the offhand
         */
        public Item getItem() {
            return item;
        }
    }
}