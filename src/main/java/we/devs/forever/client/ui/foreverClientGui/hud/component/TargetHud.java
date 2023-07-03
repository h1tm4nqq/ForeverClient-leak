package we.devs.forever.client.ui.foreverClientGui.hud.component;


import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.manager.impl.player.TargetManager;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;

public class TargetHud extends Hud {

    public Setting<Boolean> targetHudBackground = (new Setting<>("TargetHudBackground", true));
    public Setting<Color> color = (new Setting<>("BGColor", new Color(20, 20, 20, 145), ColorPickerButton.Mode.Normal,100, v -> targetHudBackground.getValue()));
    public TargetHud() {
        super("TargetHud", true);
        height = 100;
        width = 210;
    }

    public static EntityPlayer getClosestEnemy() {
        EntityPlayer closestPlayer = null;
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == mc.player) continue;
            if (friendManager.isFriend(player)) continue;
            if (closestPlayer == null) {
                closestPlayer = player;
            } else if (mc.player.getDistanceSq(player) < mc.player.getDistanceSq(closestPlayer)) {
                closestPlayer = player;
            }
        }
        return closestPlayer;
    }

    @Override
    public void onRenderHud() {
        drawTargetHud(partialTicks);
    }

    private int getBlockDamage(BlockPos pos) {
        for (DestroyBlockProgress destBlockProgress : mc.renderGlobal.damagedBlocks.values()) {
            if (destBlockProgress.getPosition().getX() == pos.getX() && destBlockProgress.getPosition().getY() == pos.getY() && destBlockProgress.getPosition().getZ() == pos.getZ()) {
                return destBlockProgress.getPartialBlockDamage();
            }
        }
        return 0;
    }

    private BlockPos traceToBlock(float partialTicks, float yaw, Entity player) {
        final Vec3d pos = EntityUtil.interpolateEntity(player, partialTicks);
        final Vec3d dir = MathUtil.direction(yaw);
        return new BlockPos(pos.x + dir.x, pos.y, pos.z + dir.z);
    }

    private Block getBlock(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();
        if ((block == Blocks.BEDROCK) || (block == Blocks.OBSIDIAN)) {
            return block;
        }
        return Blocks.AIR;
    }

    public void drawOverlay(float partialTicks, Entity player, float x, float y) {
        float yaw = 0;
        final int dir = (MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);

        switch (dir) {
            case 1:
                yaw = 90;
                break;
            case 2:
                yaw = -180;
                break;
            case 3:
                yaw = -90;
                break;
            default:
        }

        final BlockPos northPos = this.traceToBlock(partialTicks, yaw, player);
        final Block north = this.getBlock(northPos);
        if (north != null && north != Blocks.AIR) {
            final int damage = this.getBlockDamage(northPos);
            if (damage != 0) {
                RenderUtil.drawRect(x + 16, y, x + 32, y + 16, 0x60ff0000);
            }
            this.drawBlock(north, x + 16, y);
        }

        final BlockPos southPos = this.traceToBlock(partialTicks, yaw - 180.0f, player);
        final Block south = this.getBlock(southPos);
        if (south != null && south != Blocks.AIR) {
            final int damage = this.getBlockDamage(southPos);
            if (damage != 0) {
                RenderUtil.drawRect(x + 16, y + 32, x + 32, y + 48, 0x60ff0000);
            }
            this.drawBlock(south, x + 16, y + 32);
        }

        final BlockPos eastPos = this.traceToBlock(partialTicks, yaw + 90.0f, player);
        final Block east = this.getBlock(eastPos);
        if (east != null && east != Blocks.AIR) {
            final int damage = this.getBlockDamage(eastPos);
            if (damage != 0) {
                RenderUtil.drawRect(x + 32, y + 16, x + 48, y + 32, 0x60ff0000);
            }
            this.drawBlock(east, x + 32, y + 16);
        }

        final BlockPos westPos = this.traceToBlock(partialTicks, yaw - 90.0f, player);
        final Block west = this.getBlock(westPos);
        if (west != null && west != Blocks.AIR) {
            final int damage = this.getBlockDamage(westPos);

            if (damage != 0) {
                RenderUtil.drawRect(x, y + 16, x + 16, y + 32, 0x60ff0000);
            }
            this.drawBlock(west, x, y + 16);
        }
    }

    private void drawBlock(Block block, float x, float y) {
        final ItemStack stack = new ItemStack(block);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate(x, y, 0);
        mc.getRenderItem().zLevel = 501;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        mc.getRenderItem().zLevel = 0.f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    Color getColor(float range) {
        if (range <= 5.0F) {
            return new Color(232, 6, 6, 255);
        }
        if (range <= 10.0F) {
            return new Color(234, 98, 11, 255);
        }
        if (range <= 20.0F) {
            return new Color(236, 199, 11, 255);
        }
        return new Color(55, 220, 16, 255);
    }

    public void drawTargetHud(float partialTicks) {

        EntityPlayer target;
        if (AutoCrystal.getInstance().currentTarget != null && AutoCrystal.getInstance().isEnabled()) {
            target = AutoCrystal.getInstance().currentTarget;
        } else {
            target = getClosestEnemy();
        }
        if (target == null) return;
        if (targetHudBackground.getValue()) {
            RenderUtil.drawRectangleCorrectly((int) (X.getValue() + 0), (int) (Y.getValue() + 0), 210, 100, color.getValue().getRGB());
            bg = false;
        } else {
            bg = true;
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(1, 1, 1, 1);

        try {
            GuiInventory.drawEntityOnScreen((int) (X.getValue() + 30), (int) (Y.getValue() + 90), 45, 0.0f, 0.0f, target);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        renderer.drawStringWithShadow(target.getName(), X.getValue() + 60, Y.getValue() + 10, ColorUtil.toRGBA(255, 0, 0, 255));

        int healthColor;
        float health = target.getHealth() + target.getAbsorptionAmount();
        if (health >= 16.0f) {
            healthColor = ColorUtil.toRGBA(0, 255, 0, 255);
        } else if (health >= 10.0f) {
            healthColor = ColorUtil.toRGBA(255, 255, 0, 255);
        } else {
            healthColor = ColorUtil.toRGBA(255, 0, 0, 255);
        }

        DecimalFormat df = new DecimalFormat("##.#");

        renderer.drawStringWithShadow(df.format(target.getHealth() + target.getAbsorptionAmount()), X.getValue() + 60 + renderer.getStringWidth(target.getName() + "  "), Y.getValue() + 10, healthColor);

        int ping;
        if (EntityUtil.isFakePlayer(target)) {
            ping = 0;
        } else {
            Objects.requireNonNull(mc.getConnection()).getPlayerInfo(target.getUniqueID());
            ping = mc.getConnection().getPlayerInfo(target.getUniqueID()).getResponseTime();
        }

        int color;
        if (ping >= 100) {
            color = ColorUtil.toRGBA(0, 255, 0, 255);
        } else if (ping > 50) {
            color = ColorUtil.toRGBA(255, 255, 0, 255);
        } else {
            color = ColorUtil.toRGBA(255, 0, 0, 255);
        }

        renderer.drawStringWithShadow("Ping: " + ping, X.getValue() + 60, Y.getValue() + renderer.getHeight() + 15, color);
        renderer.drawStringWithShadow("Pops: " + TargetManager.getPops(target.getName()), X.getValue() + 60, Y.getValue() + renderer.getHeight() + 25, ColorUtil.toRGBA(255, 0, 0, 255));
        renderer.drawStringWithShadow("Range: " + String.format("%.1f", target.getDistance(mc.player)), X.getValue() + 60, Y.getValue() + renderer.getHeight() + 35, ColorUtil.toRGBA(getColor(Float.parseFloat(String.format("%.1f", target.getDistance(mc.player))))));

        GlStateManager.enableTexture2D();
        int iteration = 0;
        int i = (int) (X.getValue() + 80);
        int y = (int) (this.Y.getValue() + (renderer.getHeight() * 3) + 44);
        {
            ItemStack is = target.inventory.player.getHeldItemOffhand();
            iteration++;
            if (!is.isEmpty()) {
                int x = i - 90 + (9 - iteration) * 20 + 2;
                GlStateManager.enableDepth();
                RenderUtil.itemRender.zLevel = 200F;
                RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
                RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
                RenderUtil.itemRender.zLevel = 0F;
                GlStateManager.enableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                String s = is.getCount() > 1 ? is.getCount() + "" : "";
                renderer.drawStringWithShadow(s, x + 19 - 2 - renderer.getStringWidth(s), y + 9, 0xffffff);
            }
        }
        for (ItemStack is : target.inventory.armorInventory) {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            renderer.drawStringWithShadow(s, x + 19 - 2 - renderer.getStringWidth(s), y + 9, 0xffffff);
            //mc.fontRenderer.drawStringWithShadow(s, x + 19 - 2 - mc.fontRenderer.getStringWidth(s), y + 9, 0xffffff);

            int dmg = 0;
            int itemDurability = is.getMaxDamage() - is.getItemDamage();
            float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
            float red = 1 - green;
            dmg = 100 - (int) (red * 100);
            renderer.drawStringWithShadow(dmg + "", x + 8 - renderer.getStringWidth(dmg + "") / 2f, y - 5, ColorUtil.toRGBA((int) (red * 255), (int) (green * 255), 0));
        }
        {
            ItemStack is = target.inventory.getCurrentItem();
            iteration++;
            if (!is.isEmpty()) {
                int x = i - 90 + (9 - iteration) * 20 + 2;
                GlStateManager.enableDepth();
                RenderUtil.itemRender.zLevel = 200F;
                RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
                RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
                RenderUtil.itemRender.zLevel = 0F;
                GlStateManager.enableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                String s = is.getCount() > 1 ? is.getCount() + "" : "";
                renderer.drawStringWithShadow(s, x + 19 - 2 - renderer.getStringWidth(s), y + 9, 0xffffff);
            }

        }
        drawOverlay(partialTicks, target, X.getValue() + 150, this.Y.getValue() + 6);

        //   renderer.drawStringWithShadow("Strength", x.getValue() + 150, y.getValue() + 60, target.isPotionActive(MobEffects.STRENGTH) ? ColorUtil.toRGBA(0, 255, 0, 255) : ColorUtil.toRGBA(255, 0, 0, 255));

        //   renderer.drawStringWithShadow("Weakness", x.getValue() + 150, y.getValue() + renderer.getHeight() + 70, target.isPotionActive(MobEffects.WEAKNESS) ? ColorUtil.toRGBA(0, 255, 0, 255) : ColorUtil.toRGBA(255, 0, 0, 255));

    }
}
