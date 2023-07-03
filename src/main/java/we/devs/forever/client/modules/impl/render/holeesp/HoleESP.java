package we.devs.forever.client.modules.impl.render.holeesp;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.util.combat.VulnerabilityUtil;
import we.devs.forever.api.util.hole.TwoHole;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.render.util.FaceMasks;
import we.devs.forever.api.util.render.util.TessellatorUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.client.modules.impl.render.holeesp.enums.Lines;
import we.devs.forever.client.modules.impl.render.holeesp.enums.Mode;
import we.devs.forever.client.setting.Setting;  import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HoleESP extends Module {
    public static HoleESP holeESP;
    public final Setting<Integer> rangeXZ = (new Setting<>("RangeXZ", 8, 1, 25));
    public final Setting<Integer> rangeY = (new Setting<>("RangeY", 5, 1, 25));

    public final Setting<Float> width = (new Setting<>("Width", 1.5F, 0F, 10F));
    public final Setting<Float> height = (new Setting<>("Height", 1F, -2F, 8F));

    public final Setting<Mode> mode = (new Setting<>("Mode", Mode.Full));
    public final Setting<Integer> fadeAlpha = (new Setting<>("FadeAlpha", 0, 0, 255, v -> mode.getValue() == Mode.Fade));
    public final Setting<Boolean> depth = (new Setting<>("Depth", true, v -> mode.getValue() == Mode.Fade));
    public final Setting<Boolean> noLineDepth = (new Setting<>("NotLines", true, v -> mode.getValue() == Mode.Fade && depth.getValue()));
    public final Setting<Lines> lines = (new Setting<>("Lines", Lines.Bottom, v -> mode.getValue() == Mode.Fade));
    public final Setting<Boolean> sides = (new Setting<>("Sides", false, v -> mode.getValue() == Mode.Full || mode.getValue() == Mode.Fade));
    public final Setting<Boolean> notSelf = (new Setting<>("NotSelf", true, v -> mode.getValue() == Mode.Fade));

    public final Setting<Boolean> twoBlock = (new Setting<>("TwoBlock", false));

    public final Setting<Boolean> bedrock = (new Setting<>("Bedrock", true));
    public final Setting<Color> bRockHoleColor = (new Setting<>("BedrockColor", new Color(0,255,0, 102), ColorPickerButton.Mode.Normal,100, v -> bedrock.getValue()));
    public final Setting<Color> bRockLineColor = (new Setting<>("BedrockLineColor", new Color(0,255,0,255),ColorPickerButton.Mode.Normal,100, v -> bedrock.getValue()));
    public final Setting<Boolean> obsidian = (new Setting<>("Obsidian", true));
    public final Setting<Color> obiHoleColor = (new Setting<>("ObiColor", new Color(255,0,0, 111),ColorPickerButton.Mode.Normal,100, v -> obsidian.getValue()));
    public final Setting<Color> obiLineHoleColor = (new Setting<>("ObiLineColor", new Color(255,0,0,255),ColorPickerButton.Mode.Normal,100, v -> obsidian.getValue()));

    public final Setting<Boolean> vunerable = (new Setting<>("Vulnerable", false));
    public final Setting<Boolean> selfVunerable = (new Setting<>("Self", false));

    public final Setting<Color> vunerableColor = (new Setting<>("VunColor", new Color(255, 0, 255, 118),ColorPickerButton.Mode.Normal,100, v -> vunerable.getValue()));
    public final Setting<Color> vunerableLineColor = (new Setting<>("VunLineColor", new Color(255, 0, 255,255),ColorPickerButton.Mode.Normal,100, v -> vunerable.getValue()));

    List<BlockPos> obiHoles = new ArrayList<>();
    public List<BlockPos> bedrockHoles = new ArrayList<>();

    public List<TwoHole> obiHolesTwoBlock = new ArrayList<>();
    public List<TwoHole> bedrockHolesTwoBlock = new ArrayList<>();
    public static HoleESP esp;
    ModuleThread<HoleESP> updateThread = new UpdateThread(this);
    public HoleESP() {
        super("HoleESP", "hole esp", Category.RENDER);
        esp = this;
    }
    @Override
    public void onLoad() throws Throwable {
    if(isEnabled())  updateThread.start();

    }

    @Override
    public void onUnload() throws Throwable {
        updateThread.getThread().interrupt();
    }

    @Override
    public void onEnable() {
        updateThread.start();
    }

    @Override
    public void onDisable() {
        updateThread.getThread().interrupt();
    }


    @Override
    public String getDisplayInfo() {
        return mode.getValue().toString().charAt(0) + mode.getValue().toString().substring(1).toLowerCase();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (mode.getValue() == Mode.Bottom) {
            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            GlStateManager.enableBlend();
            GlStateManager.glLineWidth(width.getValue());
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            for (BlockPos pos : this.bedrockHoles) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1);

                RenderUtil.drawBoundingBox(box, bRockHoleColor.getColor().getRGB());
            }

            for (BlockPos pos : this.obiHoles) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1);

                RenderUtil.drawBoundingBox(box, obiHoleColor.getColor().getRGB());
            }

            for (TwoHole pos : this.bedrockHolesTwoBlock) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getPos().getX(), pos.  getPos().getY(), pos.  getPos().getZ(), pos.getSecondPos().getX() + 1, pos.  getSecondPos().getY(), pos.  getSecondPos().getZ() + 1);

                RenderUtil.drawBoundingBox(box, bRockHoleColor.getColor());
            }

            for (TwoHole pos : this.obiHolesTwoBlock) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.  getPos().getX(), pos.  getPos().getY(), pos.  getPos().getZ(), pos.  getSecondPos().getX() + 1, pos.  getSecondPos().getY(), pos.  getSecondPos().getZ() + 1);

                RenderUtil.drawBoundingBox(box, obiHoleColor.getColor().getRGB());
            }

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender1();
            GlStateManager.popMatrix();
        } else {
            for (BlockPos pos : this.bedrockHoles) {
                drawHole(pos, bRockHoleColor.getColor(), bRockLineColor.getColor());
            }

            for (BlockPos pos : this.obiHoles) {
                drawHole(pos, obiHoleColor.getColor(), obiLineHoleColor.getColor());
            }

            for (TwoHole pos : this.bedrockHolesTwoBlock) {
                drawHoleTwoBlock(pos.  getPos(), pos.  getSecondPos(), bRockHoleColor.getColor(), bRockLineColor.getColor());
            }

            for (TwoHole pos : this.obiHolesTwoBlock) {
                drawHoleTwoBlock(pos.  getPos(), pos.  getSecondPos(), obiHoleColor.getColor(), obiLineHoleColor.getColor());
            }
        }

        if (vunerable.getValue()) {
            List<Entity> targetsInRange = mc.world.loadedEntityList.
                    stream()
                    .filter(e -> e instanceof EntityPlayer)
                    .filter(e -> e.getDistance(mc.player) < rangeXZ.getValue())
                    .filter(e -> e != mc.player || selfVunerable.getValue())
                    .filter(e -> !friendManager.isFriend(e.getName()))
                    .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .collect(Collectors.toList());

            for (Entity target : targetsInRange) {
                ArrayList<BlockPos> vuns = VulnerabilityUtil.getVulnerablePositions(new BlockPos(target));

                for (BlockPos pos : vuns) {
                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);
                    TessellatorUtil.prepare();
                    TessellatorUtil.drawBox(axisAlignedBB, true, 1, vunerableColor.getColor(), vunerableColor.getColor().getAlpha(), FaceMasks.Quad.ALL);
                    TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), vunerableLineColor.getColor());
                    TessellatorUtil.release();
                }
            }
        }
    }

    public void drawHole(BlockPos pos, Color color, Color lineColor) {
        AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);

        axisAlignedBB = axisAlignedBB.setMaxY(axisAlignedBB.minY + height.getValue());

        if (mode.getValue() == Mode.Full) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(axisAlignedBB, true, 1, color, color.getAlpha(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            TessellatorUtil.release();
        }

        if (mode.getValue() == Mode.Full || mode.getValue() == Mode.Outline) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), lineColor);
            TessellatorUtil.release();

        }

        if (mode.getValue() == Mode.Wireframe) {
            BlockRenderUtil.prepareGL();
            BlockRenderUtil.drawWireframe(axisAlignedBB.offset(-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, -mc.getRenderManager().renderPosZ), lineColor, width.getValue());
            BlockRenderUtil.releaseGL();
        }

        if (mode.getValue() == Mode.Fade) {
            AxisAlignedBB tBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);
            tBB = tBB.setMaxY(tBB.minY + height.getValue());
            if (tBB.intersects(mc.player.getEntityBoundingBox()) && notSelf.getValue()) {
                tBB = tBB.setMaxY(Math.min(tBB.maxY, mc.player.posY + 1D));
            }

            TessellatorUtil.prepare();
            if (depth.getValue()) {
                GlStateManager.enableDepth();
                tBB = tBB.shrink(0.01D);
            }
            //RenderUtil.drawOpenGradientBox(pos,color, ColorUtil.changeAlpha(color,fadeAlpha.getValue()),height.getValue() ,depth.getValue());
            TessellatorUtil.drawBox(tBB, true, height.getValue(), color, fadeAlpha.getValue(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            if (width.getValue() >= 0.1F) {
                if (lines.getValue() == Lines.Bottom) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.minY, tBB.minZ, tBB.maxX, tBB.minY, tBB.maxZ);
                } else if (lines.getValue() == Lines.Top) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.maxY, tBB.minZ, tBB.maxX, tBB.maxY, tBB.maxZ);
                }
                if (noLineDepth.getValue()) {
                    GlStateManager.disableDepth();
                }
                TessellatorUtil.drawBoundingBox(tBB, width.getValue(), lineColor, fadeAlpha.getValue());
            }
            TessellatorUtil.release();
        }
    }


    public void drawHoleTwoBlock(BlockPos pos, BlockPos two, Color color, Color lineColor) {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), two.getX() + 1, two.getY() + height.getValue(), two.getZ() + 1);

        if (mode.getValue() == Mode.Full) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(axisAlignedBB, true, 1, color, color.getAlpha(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            TessellatorUtil.release();
        }

        if (mode.getValue() == Mode.Full || mode.getValue() == Mode.Outline) {

            TessellatorUtil.prepare();
            TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), lineColor);
            TessellatorUtil.release();

        }

        if (mode.getValue() == Mode.Wireframe) {
            BlockRenderUtil.prepareGL();
            BlockRenderUtil.drawWireframe(axisAlignedBB.offset(-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, -mc.getRenderManager().renderPosZ), lineColor, width.getValue());
            BlockRenderUtil.releaseGL();
        }

        if (mode.getValue() == Mode.Fade) {
            AxisAlignedBB tBB = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), two.getX() + 1, two.getY() + height.getValue(), two.getZ() + 1);

            if (tBB.intersects(mc.player.getEntityBoundingBox()) && notSelf.getValue()) {
                tBB = tBB.setMaxY(Math.min(tBB.maxY, mc.player.posY + 1D));
            }
            TessellatorUtil.prepare();
         //   TessellatorUtil.prepare();
            if (depth.getValue()) {
                GlStateManager.enableDepth();
                tBB = tBB.shrink(0.01D);
            }
            TessellatorUtil.drawBox(tBB, true, height.getValue(), color, fadeAlpha.getValue(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            if (width.getValue() >= 0.1F) {
                if (lines.getValue() == Lines.Bottom) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.minY, tBB.minZ, tBB.maxX, tBB.minY, tBB.maxZ);
                } else if (lines.getValue() == Lines.Top) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.maxY, tBB.minZ, tBB.maxX, tBB.maxY, tBB.maxZ);
                }
                if (noLineDepth.getValue()) {
                    GlStateManager.disableDepth();
                }
                TessellatorUtil.drawBoundingBox(tBB, width.getValue(), lineColor, fadeAlpha.getValue());
            }
            TessellatorUtil.release();
        }
    }



}
