package we.devs.forever.client.modules.impl.combat.autocrystalold.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.Mode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static we.devs.forever.api.util.Util.mc;

public class CRenderUtil {
    private static final List<RenderPos> positions = new ArrayList<>();
    private static BlockPos lastRenderPos;
    private static AxisAlignedBB renderBB;
    private static float timePassed;
    private static ResourceLocation image = new ResourceLocation("textures/icons/combat.png");

    public static void renderCa(BlockPos renderPos, float[] settings, Color boxC, Color outlineC, boolean[] settingsB, Mode renderMode, RenderMode mode) {
        boolean shoouldPlace = settingsB[0];
        boolean text = settingsB[1];
        boolean slabFactor = settingsB[2];
        boolean fadeFactor = settingsB[3];
        boolean scaleFactor = settingsB[4];
        float lineWidth = settings[0],
                range = settings[1],
                slabHeight = settings[2],
                moveSpeed = settings[3],
                renderDamage = settings[4],
                duration = settings[5],
                max = settings[6];

        if (shoouldPlace && renderPos != null ) {
            if (renderMode == Mode.Fade) {
                positions.removeIf(pos -> pos.getPos().equals(renderPos));
                positions.add(new RenderPos(renderPos, 0.0f));
            }
            if (renderMode == Mode.Static) {
                BlockRenderUtil.drawBlock(renderPos,boxC, outlineC,lineWidth, mode);
            }
            if (renderMode == Mode.Glide) {
                if (lastRenderPos == null || mc.player.getDistance(renderBB.minX, renderBB.minY, renderBB.minZ) > (double) range) {
                    lastRenderPos = renderPos;
                    renderBB = new AxisAlignedBB(renderPos);
                    timePassed = 0.0f;
                }
                if (!lastRenderPos.equals(renderPos)) {
                    lastRenderPos = renderPos;
                    timePassed = 0.0f;
                }
                double xDiff = (double) renderPos.getX() - renderBB.minX;
                double yDiff = (double) renderPos.getY() - renderBB.minY;
                double zDiff = (double) renderPos.getZ() - renderBB.minZ;
                float multiplier = timePassed / moveSpeed;
                if (multiplier > 1.0f) {
                    multiplier = 1.0f;
                }
                renderBB = renderBB.offset(xDiff * (double) multiplier, yDiff * (double) multiplier, zDiff * (double) multiplier);
                BlockRenderUtil.drawBlock(renderBB,boxC, outlineC,lineWidth, mode,1F,slabHeight);
                if (text) {
                    RenderUtil.drawText(renderBB.offset(0.0, (double) (1.0f - slabHeight / 2.0f) - 0.4, 0.0), (Math.floor(renderDamage) == renderDamage ? Integer.valueOf((int) renderDamage) : String.format("%.1f", renderDamage)) + "");

                }
                timePassed = renderBB.equals(new AxisAlignedBB(renderPos)) ? 0.0f : (timePassed += 50.0f);
            }
        }
        if (renderMode == Mode.Fade) {
            positions.forEach(pos -> {
                float factor = (duration - pos.getRenderTime()) / duration;
                BlockRenderUtil.drawBlock(new AxisAlignedBB(pos.getPos()), ColorUtil.changeAlpha(boxC, (int) Math.min(boxC.getAlpha() * factor, 255)) , ColorUtil.changeAlpha(outlineC, (int) Math.min(outlineC.getAlpha() * factor,255)),lineWidth, mode,scaleFactor ? factor : 1.0f, slabFactor ?  factor : 0.0f);
                pos.setRenderTime(pos.getRenderTime() + 50.0f);
            });
            positions.removeIf(pos -> pos.getRenderTime() >= duration || mc.world.isAirBlock(pos.getPos()) || !mc.world.isAirBlock(pos.getPos().offset(EnumFacing.UP)));
            if (positions.size() > max) {
                positions.remove(0);
            }
        }

        if (shoouldPlace && renderPos != null && text && renderMode != Mode.Glide) {
           RenderUtil.drawText(new AxisAlignedBB(renderPos).offset(0.0, renderMode != Mode.Fade ? (double) (1.0f - slabHeight / 2.0f) - 0.4 : 0.1, 0.0), (Math.floor(renderDamage) == renderDamage ? Integer.valueOf((int) renderDamage) : String.format("%.1f", renderDamage)) + "");
            //Render3dUtil.drawImageInBlock(new AxisAlignedBB(renderPos).offset(0.0, renderMode != Mode.FADE ? (double) (1.0f - slabHeight / 2.0f) - 0.4 : 0.1, 0.0), Color.cyan, Color.black, image);
        }
    }
    private static class RenderPos {
        private BlockPos renderPos;
        private float renderTime;

        public RenderPos(BlockPos pos, float time) {
            this.renderPos = pos;
            this.renderTime = time;
        }

        public BlockPos getPos() {
            return this.renderPos;
        }

        public void setPos(BlockPos pos) {
            this.renderPos = pos;
        }

        public float getRenderTime() {
            return this.renderTime;
        }

        public void setRenderTime(float time) {
            this.renderTime = time;
        }
    }
}
