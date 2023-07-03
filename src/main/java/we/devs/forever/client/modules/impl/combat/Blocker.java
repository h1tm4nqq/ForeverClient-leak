package we.devs.forever.client.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
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
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Blocker extends Module {

    public Setting<Page> page = new Setting<>("Page", Page.Place);

    public Setting<Integer> delay = (new Setting<>("Delay", 50, 0, 500, "Places blocks with delay", v -> page.getValue().equals(Page.Place)));

    public Setting<Boolean> packet = (new Setting<>("Packet", true, v -> page.getValue().equals(Page.Place)));
    public Setting<Boolean> strict = (new Setting<>("Strict", true, "Allows use blocker on strict", v -> page.getValue().equals(Page.Place)));
    public Setting<RotationType> rotate = (new Setting<>("Rotate", RotationType.Off, v -> page.getValue().equals(Page.Place)));
    public Setting<AutoSwitch> swap = new Setting<>("Swap", AutoSwitch.Silent, v -> page.getValue().equals(Page.Place));
    public Setting<Boolean> face = (new Setting<>("Face", true, "Will place blocks on surround", v -> page.getValue().equals(Page.Logic)));
    public Setting<Boolean> feet = (new Setting<>("Feet", true, "Will place blocks near surround", v -> page.getValue().equals(Page.Logic)));
    public Setting<Boolean> cornerFeet = (new Setting<>("CornerFeet", true, "Will put blocks on the surrounds corners", v -> page.getValue().equals(Page.Logic)));
    public Setting<Boolean> antiCiv = (new Setting<>("AntiCiv", true, "Places blocks over your surround blocker", v -> page.getValue().equals(Page.Logic) && face.getValue()));
    public Setting<Boolean> antiCev = (new Setting<>("AntiCev", true, "Places blocks over your autotrap", v -> page.getValue().equals(Page.Logic)));

    public Setting<Boolean> render = (new Setting<>("Render", true, v -> page.getValue().equals(Page.Render)));
    public Setting<Integer> duration = (new Setting<>("Duration", 1000, 1, 1000, v -> render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<RenderMode> renderMode = (new Setting<>("RenderMode", RenderMode.Fill, v -> render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Color> fillColor = (new Setting<>("Color", new Color(255, 255, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.Fill || renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline) && render.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));


    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final TimerUtil renderTimer = new TimerUtil(duration);
    SwitchUtil switchUtil = new SwitchUtil(swap);
    TimerUtil timerUtil = new TimerUtil(delay);
    Queue<BlockPos> blockPoss = new ArrayDeque<>();

    public Blocker() {
        super("Blocker", "Attempts to extend your surround when it's being broken.", Category.COMBAT);
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
                    Color fill = new Color(fillColor.getColor().getRed(), fillColor.getColor().getGreen(), fillColor.getColor().getBlue(), fadeBoxAlpha);
                    BlockRenderUtil.drawBlock(pos, fill, outLine, lineWidth.getValue(), renderMode.getValue());
                }
            });
        }
    }

    @EventListener
    public void onUpdate(MotionEvent.Pre event) {
        if (!fullNullCheck() && strict.getValue()) {
            for (BlockPos pos : blockPoss) {
                if (!timerUtil.passedMs()) return;
                placeBlock(pos);
            }
        }
    }

    @Override
    public void onTick() {
        if (!fullNullCheck() && !strict.getValue()) {
            for (BlockPos pos : blockPoss) {
                if (!timerUtil.passedMs()) return;
                placeBlock(pos);
            }
        }
    }
    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            if (!EntityUtil.isInHole(mc.player)) return;
            Entity entity =  mc.world.getEntityByID(((SPacketBlockBreakAnim) event.getPacket()).getBreakerId());

           if(entity != null) if(friendManager.isFriend((EntityPlayer) entity)) return;

            SPacketBlockBreakAnim packet = event.getPacket();
            BlockPos pos = packet.getPosition();

            if (mc.world.getBlockState(pos).getBlock() == (Blocks.BEDROCK) || BlockUtil.isPosEmpty(pos))
                return;
            getPos(pos);
            blockPoss = blockPoss.stream()
                    .filter(Objects::nonNull)
                    .filter(BlockUtil::isPosEmpty)
                    .filter(BlockUtil::canPlace)
                    .collect(Collectors.toCollection(ArrayDeque::new));

            if (blockPoss.isEmpty()) return;

        }

        if (event.getPacket() instanceof SPacketBlockChange) {
            if (renderBlocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() != Blocks.AIR) {

                    blockPoss.remove(((SPacketBlockChange) event.getPacket()).getBlockPosition());
                }
            }
        }
    }

    public void getPos(BlockPos pos) {
        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        if (pos.equals(playerPos.north())) {
            if (face.getValue()) blockPoss.add(playerPos.north().up());
            if (feet.getValue()) blockPoss.add(playerPos.north().north());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.north().west());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.north().east());
        }
        if (pos.equals(playerPos.east())) {
            if (face.getValue()) blockPoss.add(playerPos.east().up());
            if (feet.getValue()) blockPoss.add(playerPos.east().east());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.east().south());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.east().north());
        }
        if (pos.equals(playerPos.west())) {
            if (face.getValue()) blockPoss.add(playerPos.west().up());
            if (feet.getValue()) blockPoss.add(playerPos.west().west());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.west().south());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.west().north());
        }
        if (pos.equals(playerPos.south())) {
            if (face.getValue()) blockPoss.add(playerPos.south().up());
            if (feet.getValue()) blockPoss.add(playerPos.south().south());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.south().west());
            if (cornerFeet.getValue()) blockPoss.add(playerPos.south().east());
        }


        if (pos.equals(playerPos.up().north())) {
            if (antiCiv.getValue()) blockPoss.add(playerPos.north().up(2));
        }
        if (pos.equals(playerPos.up().east())) {
            if (antiCiv.getValue()) blockPoss.add(playerPos.east().up(2));

        }
        if (pos.equals(playerPos.up().west())) {
            if (antiCiv.getValue()) blockPoss.add(playerPos.west().up(2));

        }
        if (pos.equals(playerPos.up().south())) {
            if (antiCiv.getValue()) blockPoss.add(playerPos.south().up(2));

        }


        if (pos.equals(playerPos.up().up())) {
            if (antiCev.getValue()) blockPoss.add(playerPos.up(3));
        }
    }



    private void placeBlock(BlockPos pos) {
        if (!mc.world.isAirBlock(pos)) return;
        if(!BlockUtil.checkEntity(pos)) return;
        if (!switchUtil.check(Blocks.OBSIDIAN, Blocks.ENDER_CHEST)) return;

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityEnderCrystal) {
                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            }
        }
        switchUtil.switchTo(Blocks.OBSIDIAN, Blocks.ENDER_CHEST);
        placeManager.place(pos, rotate.getValue(), Swing.Mainhand, packet.getValue(), strict.getValue(), false, true);
        timerUtil.reset();
        blockPoss.remove(pos);

        renderBlocks.put(pos, System.currentTimeMillis());
        switchUtil.switchBack();
    }



}