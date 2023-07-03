package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class HoleHud extends Hud {
    public HoleHud() {
        super("Hole");
        width = 43;
        height = 43;
    }

    @Override
    public void onRenderHud() {
        float yaw = 0;
        final int dir = (MathHelper.floor((double) (mc.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
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
        final BlockPos northPos = this.traceToBlock(partialTicks, yaw);
        final Block north = this.getBlock(northPos);
        if (north != null && north != Blocks.AIR) {
            final int damage = this.getBlockDamage(northPos);
            if (damage != 0) {
                RenderUtil.drawRect(X.getValue() + 16, Y.getValue(), X.getValue() + 32, Y.getValue() + 16, 0x60ff0000);
            }
            this.drawBlock(north, X.getValue() + 16, Y.getValue());
        }
        final BlockPos southPos = this.traceToBlock(partialTicks, yaw - 180.0f);
        final Block south = this.getBlock(southPos);
        if (south != null && south != Blocks.AIR) {
            final int damage = this.getBlockDamage(southPos);
            if (damage != 0) {
                RenderUtil.drawRect(X.getValue() + 16, Y.getValue() + 32, X.getValue() + 32, Y.getValue() + 48, 0x60ff0000);
            }
            this.drawBlock(south, X.getValue() + 16, Y.getValue() + 32);
        }
        final BlockPos eastPos = this.traceToBlock(partialTicks, yaw + 90.0f);
        final Block east = this.getBlock(eastPos);
        if (east != null && east != Blocks.AIR) {
            final int damage = this.getBlockDamage(eastPos);
            if (damage != 0) {
                RenderUtil.drawRect(X.getValue() + 32, Y.getValue() + 16, X.getValue() + 48, Y.getValue() + 32, 0x60ff0000);
            }
            this.drawBlock(east, X.getValue() + 32, Y.getValue() + 16);
        }
        final BlockPos westPos = this.traceToBlock(partialTicks, yaw - 90.0f);
        final Block west = this.getBlock(westPos);
        if (west != null && west != Blocks.AIR) {
            final int damage = this.getBlockDamage(westPos);

            if (damage != 0) {
                RenderUtil.drawRect(X.getValue(), Y.getValue() + 16, X.getValue() + 16, Y.getValue() + 32, 0x60ff0000);
            }
            this.drawBlock(west, X.getValue(), Y.getValue() + 16);
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

    private BlockPos traceToBlock(float partialTicks, float yaw) {
        final Vec3d pos = EntityUtil.interpolateEntity(mc.player, partialTicks);
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

    private int getBlockDamage(BlockPos pos) {
        for (DestroyBlockProgress destBlockProgress : mc.renderGlobal.damagedBlocks.values()) {
            if (destBlockProgress.getPosition().getX() == pos.getX() && destBlockProgress.getPosition().getY() == pos.getY() && destBlockProgress.getPosition().getZ() == pos.getZ()) {
                return destBlockProgress.getPartialBlockDamage();
            }
        }
        return 0;
    }

}