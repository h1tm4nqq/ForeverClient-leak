package we.devs.forever.client.modules.impl.render.search;


import net.minecraft.block.Block;
import net.minecraft.block.BlockBarrier;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.player.BlockRenderEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.render.util.TessellatorUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.GL_VENDOR;

public class Search extends Module {
    private ArrayList<Block> defaultBlocks;
    public static Search INSTANCE;
    public static ArrayList<Block> customBlocks = new ArrayList<>();
    final File file = new File("Forever/util/search.json");
    private final CopyOnWriteArrayList<BlockVec> blocks = new CopyOnWriteArrayList<>();

    private final Setting<Float> range = (new Setting<>("Range", 100f, 1f, 500F));
    private final Setting<Color> color = (new Setting<>("Color", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100));

    private final Setting<Boolean> defaultSetting = (new Setting<>("Default", true));
    private final Setting<Boolean> custom = (new Setting<>("Custom", true));
    private final Setting<Boolean> illegals = (new Setting<>("Illegals", true));

    private final Setting<Boolean> tracers = (new Setting<>("Tracers", false));
    private final Setting<Boolean> fill = (new Setting<>("Fill", true));
    private final Setting<Boolean> outline = (new Setting<>("Outline", true));

    public final Setting<Boolean> softReload = (new Setting<>("SoftReload", true));
    private final Setting<Boolean> slowRender = (new Setting<>("SlowRender", false));

    private final TimerUtil timer = new TimerUtil();

    public Search() {
        super("Search", " ", Category.RENDER);
        genDefaultBlocks();
        INSTANCE = this;
    }

    public void onEnable() {
        if (GlStateManager.glGetString(GL_VENDOR).contains("Intel") && !slowRender.getValue()) {
            Command.sendMessage("You have an integrated graphics card. To increase fps, use SlowRender.");
        }
        if (softReload.getValue()) {
            doSoftReload();
        }
    }

    @Override
    public void onLoad() throws IOException {
        if (!file.exists()) {

            file.createNewFile();

        }
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            customBlocks.add(Block.getBlockById(Integer.parseInt(scanner.next())));
        }

    }

    @Override
    public void onUnload() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);
        StringBuilder stringBuilder = new StringBuilder();
        customBlocks.forEach(x -> stringBuilder.append(Block.getIdFromBlock(x)).append("\n"));
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();
    }

    public static void doSoftReload() {
        if (mc.world != null && mc.player != null) {
            int posX = (int) mc.player.posX;
            int posY = (int) mc.player.posY;
            int posZ = (int) mc.player.posZ;
            int range = mc.gameSettings.renderDistanceChunks * 16;
            mc.renderGlobal.markBlockRangeForRenderUpdate(posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range);
        }
    }

    @EventListener
    public void onBlockRender(BlockRenderEvent event) {

        if (mc.world == null || mc.player == null) return;

        if (slowRender.getValue()) {
            if (timer.passedMs(1000)) {
                timer.reset();
            } else {
                return;
            }
        }

        if (blocks.size() > 100000) {
            blocks.clear();
        }

        if (shouldAdd(event.getBlock(), event.getPos())) {
            BlockVec vec = new BlockVec(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            if (!blocks.contains(vec)) {
                blocks.add(vec);
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null || blocks.isEmpty()) return;
        if (fill.getValue() || outline.getValue()) {
            for (BlockVec vec : blocks) {
                if (vec.getDistance(new BlockVec(mc.player.posX, mc.player.posY, mc.player.posZ)) > range.getValue() || shouldRender(vec)) {
                    blocks.remove(vec);
                    continue;
                }

                BlockPos pos = new BlockPos(vec.x, vec.y, vec.z);

                AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);

                if (fill.getValue()) {
                    TessellatorUtil.prepare();
                    TessellatorUtil.drawBox(axisAlignedBB, color.getValue());
                    TessellatorUtil.release();
                }

                if (outline.getValue()) {
                    TessellatorUtil.prepare();
                    TessellatorUtil.drawBoundingBox(axisAlignedBB, 1.5F, color.getValue());
                    TessellatorUtil.release();
                }
            }
        }

        if (tracers.getValue()) {
            for (BlockVec vec : blocks) {
                if (vec.getDistance(new BlockVec(mc.player.posX, mc.player.posY, mc.player.posZ)) > range.getValue() || shouldRender(vec)) {
                    blocks.remove(vec);
                    continue;
                }

                Vec3d eyes = new Vec3d(0, 0, 1)
                        .rotatePitch(-(float) Math
                                .toRadians(mc.player.rotationPitch))
                        .rotateYaw(-(float) Math
                                .toRadians(mc.player.rotationYaw));

                renderTracer(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z,
                        vec.x - mc.getRenderManager().renderPosX + 0.5,
                        vec.y - mc.getRenderManager().renderPosY+ 0.5,
                        vec.z - mc.getRenderManager().renderPosZ + 0.5,
                        color.getColor());
            }
        }
    }

    private boolean shouldAdd(Block block, BlockPos pos) {
        if (defaultSetting.getValue()) {
            if (defaultBlocks.contains(block)) {
                return true;
            }
        }

        if (custom.getValue()) {
            if (customBlocks.contains(block)) {
                return true;
            }
        }

        if (illegals.getValue()) {
            return isIllegal(block, pos);
        }

        return false;
    }

    private boolean shouldRender(BlockVec vec) {
        if (defaultSetting.getValue()) {
            if (defaultBlocks.contains(mc.world.getBlockState(new BlockPos(vec.x, vec.y, vec.z)).getBlock())) {
                return false;
            }
        }

        if (custom.getValue()) {
            if (customBlocks.contains(mc.world.getBlockState(new BlockPos(vec.x, vec.y, vec.z)).getBlock())) {
                return false;
            }
        }

        if (illegals.getValue()) {
            return !isIllegal(mc.world.getBlockState(new BlockPos(vec.x, vec.y, vec.z)).getBlock(), new BlockPos(vec.x, vec.y, vec.z));
        }

        return true;
    }

    private boolean isIllegal(Block block, BlockPos pos) {
        if (block instanceof BlockCommandBlock || block instanceof BlockBarrier) return true;

        if (block == Blocks.BEDROCK) {
            if (mc.player.dimension == 0) {
                return pos.getY() > 4;
            } else if (mc.player.dimension == -1) {
                return pos.getY() > 127 || (pos.getY() < 123 && pos.getY() > 4);
            } else {
                return true;
            }
        }
        return false;
    }

    public static void renderTracer(double x, double y, double z, double x2, double y2, double z2, Color color) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();

       mc.entityRenderer.orientCamera(mc.getRenderPartialTicks());

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3d(1d, 1d, 1d);
        GlStateManager.enableLighting();
    }

    private static class BlockVec {
        public final double x;
        public final double y;
        public final double z;

        public BlockVec(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean equals(Object object) {
            if (object instanceof BlockVec) {
                BlockVec v = (BlockVec) object;
                return Double.compare(x, v.x) == 0 && Double.compare(y, v.y) == 0 && Double.compare(z, v.z) == 0;
            }
            return super.equals(object);
        }

        public double getDistance(BlockVec v) {
            double dx = x - v.x;
            double dy = y - v.y;
            double dz = z - v.z;

            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    private void genDefaultBlocks() {
        defaultBlocks = new ArrayList<>();
        defaultBlocks.add(Blocks.PORTAL);
        defaultBlocks.add(Blocks.MOB_SPAWNER);
        defaultBlocks.add(Blocks.END_PORTAL_FRAME);
        defaultBlocks.add(Blocks.END_PORTAL);
        defaultBlocks.add(Blocks.DISPENSER);
        defaultBlocks.add(Blocks.DROPPER);
        defaultBlocks.add(Blocks.HOPPER);
        defaultBlocks.add(Blocks.FURNACE);
        defaultBlocks.add(Blocks.LIT_FURNACE); // lit
        defaultBlocks.add(Blocks.CHEST);
        defaultBlocks.add(Blocks.TRAPPED_CHEST);
        defaultBlocks.add(Blocks.ENDER_CHEST);
        defaultBlocks.add(Blocks.WHITE_SHULKER_BOX);
        defaultBlocks.add(Blocks.ORANGE_SHULKER_BOX);
        defaultBlocks.add(Blocks.MAGENTA_SHULKER_BOX);
        defaultBlocks.add(Blocks.LIGHT_BLUE_SHULKER_BOX);
        defaultBlocks.add(Blocks.YELLOW_SHULKER_BOX);
        defaultBlocks.add(Blocks.LIME_SHULKER_BOX);
        defaultBlocks.add(Blocks.PINK_SHULKER_BOX);
        defaultBlocks.add(Blocks.GRAY_SHULKER_BOX);
        defaultBlocks.add(Blocks.SILVER_SHULKER_BOX);
        defaultBlocks.add(Blocks.CYAN_SHULKER_BOX);
        defaultBlocks.add(Blocks.PURPLE_SHULKER_BOX);
        defaultBlocks.add(Blocks.BLUE_SHULKER_BOX);
        defaultBlocks.add(Blocks.BROWN_SHULKER_BOX);
        defaultBlocks.add(Blocks.GREEN_SHULKER_BOX);
        defaultBlocks.add(Blocks.RED_SHULKER_BOX);
        defaultBlocks.add(Blocks.BLACK_SHULKER_BOX);
    }
}
