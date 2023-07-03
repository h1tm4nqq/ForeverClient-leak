
/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockAir
 *  net.minecraft.block.BlockEndPortalFrame
 *  net.minecraft.block.BlockObsidian
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.CPacketChatMessage
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3d
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package we.devs.forever.client.modules.impl.combat;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.Page;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoTrap
extends Module {
    public static boolean isPlacing;
    private final Setting<Page> page = (new Setting<>("Page", Page.Place));
    private final Setting<Integer> delay = (new Setting<>("Delay/Place", 50, 0, 250,v -> page.getValue().equals(Page.Place)));
    private final Setting<Integer> blocksPerPlace = (new Setting<>("Block/Place", 8, 1, 30,v -> page.getValue().equals(Page.Place)));
    private final Setting<Double> range = (new Setting<>("PlaceRange", 6.0, 0.0, 10.0,v -> page.getValue().equals(Page.Place)));
    private final Setting<RotationType> rotate = (new Setting<>("Rotate", RotationType.Legit,v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> raytrace = (new Setting<>("Raytrace", false,v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> packet = (new Setting<>("Packet", false,v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> strict = (new Setting<>("Strict", false,v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> nolag = (new Setting<>("Nolag", false,v -> page.getValue().equals(Page.Place)));
    private final Setting<AutoSwitch> switchMode = (new Setting<>("Switch", AutoSwitch.Silent,v -> page.getValue().equals(Page.Place)));
    private final Setting<Double> targetRange = (new Setting<>("TargetRange", 10.0, 0.0, 20.0,v -> page.getValue().equals(Page.Logic)));
    private final Setting<TargetMode> targetMode = (new Setting<>("Target", TargetMode.Closest,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Pattern> pattern = (new Setting<>("Pattern", Pattern.Static,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Integer> extend = (new Setting<>("Extend", 4, 1, 4, "Extending the Trap.", v -> pattern.getValue() == Pattern.Smart && page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> antiScaffold = (new Setting<>("AntiScaffold", false ,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> antiStep = (new Setting<>("AntiStep", false,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> face = (new Setting<>("Face", true,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> legs = (new Setting<>("Legs", false, v -> pattern.getValue() != Pattern.Smart && page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> platform = (new Setting<>("Platform", false, v -> pattern.getValue() != Pattern.Smart  && page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> antiDrop = (new Setting<>("AntiDrop", false,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> antiSelf = (new Setting<>("AntiSelf", false,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> noScaffoldExtend = (new Setting<>("NoScaffoldExtend", false,v -> page.getValue().equals(Page.Logic)));
    private final Setting<Boolean> render = (new Setting<>("Render", true, v ->page.getValue().equals(Page.Render)));
    public final Setting<Integer> duration = (new Setting<>("Duration", 1000, 1, 1000, v -> render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<RenderMode> renderMode = (new Setting<>("RenderMode", RenderMode.Fill, v -> render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Color> fillColor = (new Setting<>("Color", new Color(255, 255, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.Fill || renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline) && render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));

    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    SwitchUtil switchUtil = new SwitchUtil(switchMode);
    private final TimerUtil timer = new TimerUtil();
    public EntityPlayer target;
    private boolean didPlace;
    private int placements;

    public AutoTrap() {
        super("AutoTrap", "Traps other players.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable();
        }
    }

    @Override
    public void onLogout() {
        disable();
    }




    @EventListener
    public void onUpdateWalkingPlayer(MotionEvent.Pre event) {
      doTrap();
    }



    @Override
    public String getDisplayInfo() {
        if ( target != null) {
            return target.getName();
        }
        return null;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        isPlacing = false;
    }

    @Override
    public void onAltRender3D(float ticks) {

        if (render.getValue()) {

            renderBlocks.forEach((pos, time) -> {
                if (System.currentTimeMillis() > time + duration.getValue()) {
                    renderBlocks.remove(pos);
                } else {

                    final float maxBoxAlpha = fillColor.getColor().getAlpha();
                    final float maxOutlineAlpha = outLineColor.getColor().getAlpha();

                    float alphaBoxAmount = maxBoxAlpha / this.duration.getValue();
                    float alphaOutlineAmount = maxOutlineAlpha / this.duration.getValue();

                    int fadeBoxAlpha = MathUtil.clamp((int) (alphaBoxAmount * (time + duration.getValue() - System.currentTimeMillis())), 0, (int) maxBoxAlpha);
                    int fadeOutlineAlpha = MathUtil.clamp((int) (alphaOutlineAmount * (time + duration.getValue() - System.currentTimeMillis())), 0, (int) maxOutlineAlpha);

                    Color outLine = new Color(outLineColor.getColor().getRed(), outLineColor.getColor().getGreen(), outLineColor.getColor().getBlue(), fadeOutlineAlpha);
                    Color fill = new Color(fillColor.getColor().getRed(), fillColor.getColor().getGreen(), fillColor.getColor().getBlue(),fadeBoxAlpha );
                    BlockRenderUtil.drawBlock(pos, fill, outLine, lineWidth.getValue(), renderMode.getValue());
                }
            });
        }
    }
    

    private void doTrap() {
        if (check()) {
            return;
        }
        switch (pattern.getValue()) {
            case Static: {
                doStaticTrap();
                break;
            }
            case Smart: {
                doSmartTrap();
            }
        }
        if (didPlace) {
            timer.reset();
        }
    }

    private void doSmartTrap() {
        List<Vec3d> placeTargets = EntityUtil.getUntrappedBlocksExtended(extend.getValue(), target, antiScaffold.getValue(), antiStep.getValue(), legs.getValue(), platform.getValue(), antiDrop.getValue(), raytrace.getValue(), noScaffoldExtend.getValue(), face.getValue());
        placeList(placeTargets);
    }

    private void doStaticTrap() {
        List<Vec3d> placeTargets = EntityUtil.targets(target.getPositionVector(), antiScaffold.getValue(), antiStep.getValue(), legs.getValue(), platform.getValue(), antiDrop.getValue(), raytrace.getValue(), face.getValue());
        placeList(placeTargets);
    }

    private void placeList(List<Vec3d> list) {
        list.sort((vec3d, vec3d2) -> Double.compare(mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
        list.sort(Comparator.comparingDouble(vec3d -> vec3d.y));
        for (Vec3d vec3d3 : list) {
            BlockPos position = new BlockPos(vec3d3);
            int placeability = BlockUtil.isPositionPlaceable(position, raytrace.getValue());

            if (placeability == 1) {
                placeBlock(position);
                continue;
            }
            if (placeability != 3 || antiSelf.getValue() && areVec3dsAligned(mc.player.getPositionVector(), vec3d3)) continue;
            placeBlock(position);
        }
    }

    private boolean check() {
        isPlacing = false;
        didPlace = false;
        placements = 0;
        if (!switchUtil.check(Blocks.BEDROCK,Blocks.OBSIDIAN,Blocks.ENDER_CHEST,Blocks.END_PORTAL_FRAME)) {
                disable("You are out of Obsidian/EnderChest/Bedrock");
            return true;
        }
        target = getTarget(targetRange.getValue(), targetMode.getValue() == TargetMode.UnTrapped);
        return target == null || !timer.passedMs(delay.getValue());
    }

    private EntityPlayer getTarget(double range, boolean trapped) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;
        for (EntityPlayer player : mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, range) || pattern.getValue() == Pattern.Static && trapped && EntityUtil.isTrapped(player, antiScaffold.getValue(), antiStep.getValue(), legs.getValue(), platform.getValue(), antiDrop.getValue(), face.getValue()) || pattern.getValue() != Pattern.Static && trapped && EntityUtil.isTrappedExtended(extend.getValue(), player, antiScaffold.getValue(), antiStep.getValue(), legs.getValue(), platform.getValue(), antiDrop.getValue(), raytrace.getValue(), noScaffoldExtend.getValue(), face.getValue()) || EntityUtil.getRoundedBlockPos(mc.player).equals(EntityUtil.getRoundedBlockPos(player)) && antiSelf.getValue()) continue;
            if (target == null) {
                target = player;
                distance = mc.player.getDistanceSq(player);
                continue;
            }
            if (!(mc.player.getDistanceSq(player) < distance)) continue;
            target = player;
            distance = mc.player.getDistanceSq(player);
        }
        return target;
    }

    private void placeBlock(BlockPos pos) {
        if (placements < blocksPerPlace.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(range.getValue())) {
            switchUtil.switchTo(Blocks.BEDROCK,Blocks.OBSIDIAN,Blocks.ENDER_CHEST,Blocks.END_PORTAL_FRAME);
            placeManager.place(pos,rotate.getValue(), Swing.Mainhand,packet.getValue(),strict.getValue(),nolag.getValue(),true);
            switchUtil.switchBack();
            isPlacing = true;
            renderBlocks.put(pos, System.currentTimeMillis());
            didPlace = true;
            ++placements;

        }
    }
    public static boolean areVec3dsAligned(Vec3d vec3d1, Vec3d vec3d2) {
        return MathUtil.areVec3dsAlignedRetarded(vec3d1, vec3d2);
    }


    public  enum Pattern {
        Static,
        Smart,

    }

    public  enum TargetMode {
        Closest,
        UnTrapped

    }

}

