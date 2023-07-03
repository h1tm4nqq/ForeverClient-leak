package we.devs.forever.client.modules.impl.render;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BurrowHighlight
        extends Module {
    public static BurrowHighlight burrowHighlight;
    private final Setting<Integer> range = (new Setting<>("Range", 10, 0, 100, "How far will client show you that person in burrow"));
    private final Setting<Boolean> nameBlock = (new Setting<>("NameBlock", false, "Shows you in what block the person burrowed"));
    private final Setting<RenderMode> mode = (new Setting<>("RenderMode", RenderMode.Fill, "Render mode"));
    private final Setting<Color> fillColor = (new Setting<>("Color", new Color(255, 255, 255, 100), ColorPickerButton.Mode.Normal, 100, "BurrowHighlight color", v -> mode.getValue() == RenderMode.Fill || mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Wireframe));
    private final Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, "BurrowHighlight outline color", v -> mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Outline));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, "Width of BurrowHighlight lines", v -> mode.getValue() == RenderMode.FillOutline || mode.getValue() == RenderMode.Outline || mode.getValue() == RenderMode.Wireframe));

    List<Block> blockList = new ArrayList<>();

    public BurrowHighlight() {
        super("BurrowHighlight", "Shows you people in burrow", Category.RENDER);
        burrowHighlight = this;
        blockList.add(Blocks.GOLDEN_RAIL);
        blockList.add(Blocks.DETECTOR_RAIL);
        blockList.add(Blocks.TALLGRASS);
        blockList.add(Blocks.DEADBUSH);
        blockList.add(Blocks.YELLOW_FLOWER);
        blockList.add(Blocks.RED_FLOWER);
        blockList.add(Blocks.BROWN_MUSHROOM);
        blockList.add(Blocks.RED_MUSHROOM);
        blockList.add(Blocks.TORCH);
        blockList.add(Blocks.FIRE);
        blockList.add(Blocks.WHEAT);
        blockList.add(Blocks.STANDING_SIGN);
        blockList.add(Blocks.RAIL);
        blockList.add(Blocks.LEVER);
        blockList.add(Blocks.UNLIT_REDSTONE_TORCH);
        blockList.add(Blocks.REDSTONE_TORCH);
        blockList.add(Blocks.SNOW_LAYER);
        blockList.add(Blocks.REEDS);
        blockList.add(Blocks.CAKE);
        blockList.add(Blocks.UNPOWERED_REPEATER);
        blockList.add(Blocks.POWERED_REPEATER);
        blockList.add(Blocks.BROWN_MUSHROOM_BLOCK);
        blockList.add(Blocks.RED_MUSHROOM_BLOCK);
        blockList.add(Blocks.ACACIA_FENCE_GATE);
        blockList.add(Blocks.DARK_OAK_FENCE_GATE);
        blockList.add(Blocks.JUNGLE_FENCE_GATE);
        blockList.add(Blocks.BIRCH_FENCE_GATE);
        blockList.add(Blocks.SPRUCE_FENCE_GATE);
        blockList.add(Blocks.OAK_FENCE_GATE);
        blockList.add(Blocks.COCOA);
        blockList.add(Blocks.TRIPWIRE_HOOK);
        blockList.add(Blocks.TRIPWIRE);
        blockList.add(Blocks.FLOWER_POT);
        blockList.add(Blocks.CARROTS);
        blockList.add(Blocks.WOODEN_BUTTON);
        blockList.add(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        blockList.add(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        blockList.add(Blocks.UNPOWERED_COMPARATOR);
        blockList.add(Blocks.POWERED_COMPARATOR);
        blockList.add(Blocks.DAYLIGHT_DETECTOR);
    }


    @Override
    public void onRender3D(Render3DEvent event) {
        mc.world.playerEntities.stream()
                .filter(x -> EntityUtil.isValid(x, range.getValue()))
                .filter(pss -> {
                    if (Blocks.AIR.equals(mc.world.getBlockState(new BlockPos(pss.posX, pss.posY, pss.posZ)).getBlock())) {
                        return false;
                    }
                    if(blockList.contains(mc.world.getBlockState(new BlockPos(pss.posX, pss.posY, pss.posZ)).getBlock()))
                        return false;

                    if (Blocks.WATER.equals(mc.world.getBlockState(new BlockPos(pss.posX, pss.posY, pss.posZ)).getBlock())) {
                        return false;
                    }
                    return !Blocks.LAVA.equals(mc.world.getBlockState(new BlockPos(pss.posX, pss.posY, pss.posZ)).getBlock());
                })
                .forEach(x -> {
                    //Render3dUtil.drawImageInBlock(new AxisAlignedBB(new BlockPos(MathHelper.floor(x.posX),MathHelper.floor(x.posY), MathHelper.floor(x.posZ))), Color.cyan, Color.black, new ResourceLocation("/textures/icons/combat.png"));
             BlockPos poss = new BlockPos(MathHelper.floor(x.posX), MathHelper.floor(x.posY), MathHelper.floor(x.posZ));
                if(nameBlock.getValue()) RenderUtil.drawText(poss, mc.world.getBlockState(poss).getBlock().getLocalizedName(),1F,Color.WHITE);
                    BlockRenderUtil.drawBlock(poss, fillColor.getColor(), outLineColor.getColor(), lineWidth.getValue(), mode.getValue());
                });
    }


}

