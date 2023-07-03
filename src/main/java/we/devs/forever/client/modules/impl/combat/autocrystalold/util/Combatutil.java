package we.devs.forever.client.modules.impl.combat.autocrystalold.util;

import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import static we.devs.forever.api.util.Util.mc;

public class Combatutil {
    public static boolean isArmorUnderPercent(EntityPlayer player, float percent) {
        for (int i = 3; i >= 0; --i) {
            final ItemStack stack = player.inventory.armorInventory.get(i);
            if (getDamageInPercent(stack) < percent) {
                return true;
            }
        }
        return false;
    }

    public static boolean canSee(BlockPos blockPos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5), false, true, false) == null;
    }

    public static boolean shouldHoldFacePlace(boolean lol) {
        if (mc.currentScreen instanceof GuiOptions
                || mc.currentScreen instanceof GuiVideoSettings
                || mc.currentScreen instanceof GuiScreenOptionsSounds
                || mc.currentScreen instanceof GuiContainer
                || mc.currentScreen instanceof GuiIngameMenu
                || mc.currentScreen instanceof ForeverClientGui
        ) {
            return false;
        }
        return lol && Mouse.isButtonDown(0);
    }

    public static float getDamageInPercent(ItemStack stack) {
        float green = ((float) stack.getMaxDamage() - (float) stack.getItemDamage()) / (float) stack.getMaxDamage();
        float red = 1.0f - green;
        return 100 - (int) (red * 100.0f);
    }


    @SuppressWarnings("all")
    public static boolean shouldAntiWeakness() {
        if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
            if (mc.player.isPotionActive(MobEffects.STRENGTH)) {
                if (mc.player.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier() >= 2) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static void SwingArm(Swing swing) {
        switch (swing) {
            case Mainhand:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                return;
            case Offhand:
                mc.player.swingArm(EnumHand.OFF_HAND);
                return;
            case None:
                // PlayerUtil.send(new CPacketPlayer());
        }

    }

    public static boolean isVisible(Vec3d vec3d) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox()).minY + mc.player.getEyeHeight(), mc.player.posZ);
        return mc.world.rayTraceBlocks(eyesPos, vec3d) == null;
    }

    public static int AntiWeaknessFind() {
        return InventoryUtil.getItemHotbar(Items.DIAMOND_SWORD);
    }


}
