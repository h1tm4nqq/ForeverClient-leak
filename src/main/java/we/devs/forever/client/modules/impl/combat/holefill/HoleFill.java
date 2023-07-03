package we.devs.forever.client.modules.impl.combat.holefill;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.impl.player.holeManager.thread.holes.HoleObserver;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.NCPUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.PredictPlayer;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.hole.HoleUtil;
import we.devs.forever.api.util.math.PositionUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.Page;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class HoleFill extends Module implements HoleObserver {

    public static HoleFill INSTANCE;


    public Setting<Page> page = new Setting<>("Page", Page.Place);
    public Setting<Integer> calcDelay = (new Setting<>("CalcDelay", 4, 0, 30, v -> page.getValue().equals(Page.Place)));
    public Setting<Integer> delay = (new Setting<>("Delay", 4, 0, 30, v -> page.getValue().equals(Page.Place)));
    public Setting<Integer> bpt = (new Setting<>("BPT", 4, 0, 30, v -> page.getValue().equals(Page.Place)));
    public Setting<RotationType> rotate = (new Setting<>("Rotate", RotationType.Off, v -> page.getValue().equals(Page.Place)));
    public Setting<Swing> swing = (new Setting<>("Swing", Swing.Mainhand, v -> page.getValue().equals(Page.Place)));
    public Setting<Boolean> packet = (new Setting<>("PacketPlace", false, v -> page.getValue().equals(Page.Place)));
    public Setting<Boolean> strict = (new Setting<>("Strict", false, v -> page.getValue().equals(Page.Place)));
    public Setting<Boolean> boost = (new Setting<>("NoLag", true, "Boost speed place.", v -> page.getValue().equals(Page.Place)));
    public Setting<Float> range = (new Setting<>("Range", 4.5F, 0.1F, 6.0F, v -> page.getValue().equals(Page.Place)));
    public Setting<Float> wallRange = (new Setting<>("WallRange", 3F, 0.1F, 6.0F, v -> page.getValue().equals(Page.Place)));
    public Setting<Boolean> checkRange = (new Setting<>("CheckRange", true, "Debug.", v -> page.getValue().equals(Page.Place)));
    public Setting<AutoSwitch> silentSwitch = (new Setting<>("Switch", AutoSwitch.Silent, v -> page.getValue().equals(Page.Place)));
    public Setting<Boolean> debug = (new Setting<>("Debug", true, "Debug.", v -> page.getValue().equals(Page.Place)));


    public Setting<Boolean> smart = (new Setting<>("Smart", false, v -> page.getValue().equals(Page.Logic)));
    public Setting<Integer> extrapolationTicks = new Setting<>("ExtrapolationTicks", 0, 0, 15, v -> page.getValue().equals(Page.Logic) && smart.getValue());
    public Setting<Integer> selfExtrapolationTicks = new Setting<>("SelfExtrapolationTicks", 0, 0, 15, v -> page.getValue().equals(Page.Logic) && smart.getValue());
    public Setting<Boolean> selfSmart = (new Setting<>("SelfSmart", false, v -> page.getValue().equals(Page.Logic) && smart.getValue()));
    public Setting<Float> smartRange = (new Setting<>("Smart Range", 2f, 0f, 6f, v -> page.getValue().equals(Page.Logic) && smart.getValue()));

    public Setting<Boolean> renderSetting = (new Setting<>("Render", true, v -> page.getValue().equals(Page.Render)));
    public Setting<Integer> duration = (new Setting<>("Duration", 1000, 1, 1000, v -> renderSetting.getValue() && page.getValue().equals(Page.Render)));
    public Setting<RenderMode> renderMode = (new Setting<>("RenderMode", RenderMode.Fill, v -> renderSetting.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Color> fillColor = (new Setting<>("Color", new Color(255, 255, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.Fill || renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Wireframe) && renderSetting.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline) && renderSetting.getValue() && page.getValue().equals(Page.Render)));
    public Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline || renderMode.getValue() == RenderMode.Wireframe) && renderSetting.getValue() && page.getValue().equals(Page.Render)));

    int bpt1 = 0;
    public SwitchUtil switchUtil = new SwitchUtil(silentSwitch);
    public List<BlockPos> placePos = new CopyOnWriteArrayList<>();
    public EntityPlayer closestTarget = null;
    public TimerUtil placeTimer = new TimerUtil();
    public TimerUtil calcTimer = new TimerUtil();
    boolean isRotate = false;
    final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    float yaw, pitch;
    final CalcThread calcThread = new CalcThread(this);
    final NCPUtil ncpUtil = new NCPUtil();
    protected List<BlockPos> safes = Collections.emptyList();
    protected List<BlockPos> unsafes = Collections.emptyList();
    protected List<BlockPos> longs = Collections.emptyList();
    protected List<BlockPos> bigs = Collections.emptyList();


    public HoleFill() {
        super("HoleFill", "Fills holes around you.", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
     //   calcThread.start();
        holeManager.register(this);
    }

    @Override
    public void onDisable() {
        closestTarget = null;
        bpt1 = 0;
     //   calcThread.getThread().interrupt();
        holeManager.unregister(this);
    }

    @Override
    public void onLoad() throws Throwable {
        if (isEnabled()) calcThread.start();
        if (this.isEnabled()) {
            holeManager.register(this);
        }
    }

    @Override
    public void onUnload() throws Throwable {
       // calcThread.getThread().interrupt();
    }

    public static HoleFill getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HoleFill();
        }
        return INSTANCE;
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    @Override
    public void onThread() {
        if (fullNullCheck()) return;

        if (ncpUtil.passedInTicks(calcDelay.getValue())) {
            calc();

            if (!strict.getValue()) {
                doHoleFill();
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck() || nullCheck() || placePos.isEmpty()) {
            return;
        }
        if (event.getPacket() instanceof SPacketBlockChange) {
            BlockPos changePos = ((SPacketBlockChange) event.getPacket()).getBlockPosition();

            if (((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) {

                if (BlockUtil.canPlace(changePos) && placePos.contains(changePos)) {
                    if (switchUtil.switchTo(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.WEB, Blocks.ANVIL)) {
                        placeManager.place(changePos, rotate.getValue(), swing.getValue(), packet.getValue(), strict.getValue(), boost.getValue(), true);
                        bpt1++;
                        renderBlocks.put(changePos, System.currentTimeMillis());
                        placePos.remove(changePos);
                        switchUtil.switchBack();
                    }
                }
            }

        }
        if (event.getPacket() instanceof SPacketMultiBlockChange) {

            for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks()) {

                BlockPos changePos = blockUpdateData.getPos();

                if (blockUpdateData.getBlockState().getMaterial().isReplaceable() && BlockUtil.canPlace(changePos) && placePos.contains(changePos)) {

                    if (switchUtil.switchTo(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.WEB, Blocks.ANVIL))
                        placeManager.place(changePos, rotate.getValue(), swing.getValue(), packet.getValue(), strict.getValue(), boost.getValue(), true);
                    renderBlocks.put(changePos, System.currentTimeMillis());
                    bpt1++;
                    placePos.remove(changePos);
                }
            }
        }

    }


    @EventListener
    public void onUpdate(MotionEvent.Pre event) {
        if (fullNullCheck()) return;

        if (ncpUtil.passedInTicks(calcDelay.getValue())) calc();

        if (strict.getValue()) {
            doHoleFill();
        }
    }

    @Override
    public void onTick() {
        bpt1 = 0;
    }

    public void doHoleFill() {
        if (fullNullCheck() || nullCheck() || placePos.isEmpty()) {
            return;
        }
        for (BlockPos pos : placePos) {
            if (!BlockUtil.isPosEmpty(pos)) continue;
            if (!BlockUtil.canPlace(pos)) continue;
            if (!placeTimer.passedMs(delay.getValue())) break;
            if (bpt1 >= bpt.getValue()) break;
            if (switchUtil.switchTo(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ANVIL)) {
                float[] angles = rotationManager.getAngle(new Vec3d(pos));
                yaw = angles[0];
                pitch = angles[1];
                isRotate = true;
                placeManager.place(pos, RotationType.Off, Swing.Mainhand, packet.getValue(), strict.getValue(), boost.getValue(), true);
                renderBlocks.put(pos, System.currentTimeMillis());
                bpt1++;
                switchUtil.switchBack();
                placeTimer.reset();
            }
        }
        if (isRotate) {
            if (strict.getValue()) rotationManager.setStrict();
            rotationManager.doRotation(rotate.getValue(), yaw, pitch);
            isRotate = false;
        }

    }

    public void calc() {
        BlockPos playerPos = PositionUtil.getPosition();
//        if (calcTimer.passedMs(calcDelay.getValue()))
//        {
//            HoleRunnable runnable = new HoleRunnable(this, this);
//            runnable.run();
//            calcTimer.reset();
//        }

        List<BlockPos> all = new ArrayList<>(safes.size()
                + unsafes.size()
                + longs.size()
                + bigs.size());

        all.addAll(safes);
        all.addAll(unsafes);
        all.addAll(longs);
        all.addAll(bigs);

        List<PredictPlayer> targets = new LinkedList<>(mc.world.playerEntities).stream()
                .filter(player -> EntityUtil.isValid(player, range.getValue()))
                .map(entityPlayer -> extrapolationTicks.getValue() > 0 ? MathUtil.predictPlayer(entityPlayer, extrapolationTicks.getValue()) : new PredictPlayer(entityPlayer, entityPlayer))
                .collect(Collectors.toCollection(LinkedList::new));
        if (targets.isEmpty()) return;
        PredictPlayer self = selfExtrapolationTicks.getValue() > 0 ? MathUtil.predictPlayer(mc.player, selfExtrapolationTicks.getValue()) : new PredictPlayer(mc.player, mc.player);

        List<BlockPos> holes = holeManager.getHoleList().stream()
                .filter(BlockUtil::isPosEmpty)
                .filter(BlockUtil::checkEntity)
                .filter(blockPos -> !HoleUtil.isHole(playerPos, false)[0]
                        && !HoleUtil.is2x1(playerPos)
                        && !HoleUtil.is2x2(playerPos))
//                .filter(blockPos -> placeManager.inRange(blockPos, strict.getValue(), range.getValue(), wallRange.getValue()))
                .collect(Collectors.toList());
        holes.removeIf(blockPos -> placeManager.inRange(blockPos, strict.getValue(), range.getValue(), wallRange.getValue()));

        Map<BlockPos, PredictPlayer> blockPoss = new HashMap<>();
        double length = Double.MAX_VALUE;
        for (PredictPlayer predictPlayer : targets)
            for (BlockPos pos : holes) {
                if (predictPlayer.getTarget().getDistanceSq(pos) > smartRange.getValue() * smartRange.getValue())
                    continue;
                if (selfSmart.getValue() && self.getTarget().getDistanceSq(pos) <= smartRange.getValue() * smartRange.getValue())
                    continue;
                if (length > self.getOldPlayer().getDistanceSq(pos)) {
                    length = self.getTarget().getDistanceSq(pos);
                    blockPoss.put(pos, predictPlayer);
                }
            }
        placePos = blockPoss.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().getTarget().getDistanceSq(mc.player)))
                .sorted(Comparator.comparing(entry -> entry.getValue().getTarget().getDistanceSq(entry.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void onAltRender3D(float ticks) {
        if (fullNullCheck() || nullCheck()) {
            return;
        }
        if (renderSetting.getValue()) {

            renderBlocks.forEach((pos, time) -> {
                if (pos == null) {
                    renderBlocks.remove(null);
                    return;
                }
                if (System.currentTimeMillis() > time + duration.getValue()) {
                    renderBlocks.remove(pos);
                } else {

                    final float maxBoxAlpha = fillColor.getColor().getAlpha();
                    final float maxOutlineAlpha = outLineColor.getColor().getAlpha();

                    float alphaBoxAmount = maxBoxAlpha / this.duration.getValue();
                    float alphaOutlineAmount = maxOutlineAlpha / this.duration.getValue();

                    int fadeBoxAlpha = we.devs.forever.api.util.client.MathUtil.clamp((int) (alphaBoxAmount * (time + duration.getValue() - System.currentTimeMillis())), 0, (int) maxBoxAlpha);
                    int fadeOutlineAlpha = MathUtil.clamp((int) (alphaOutlineAmount * (time + duration.getValue() - System.currentTimeMillis())), 0, (int) maxOutlineAlpha);

                    Color outLine = new Color(outLineColor.getColor().getRed(), outLineColor.getColor().getGreen(), outLineColor.getColor().getBlue(), fadeOutlineAlpha);
                    Color fill = new Color(fillColor.getColor().getRed(), fillColor.getColor().getGreen(), fillColor.getColor().getBlue(), fadeBoxAlpha);
                    BlockRenderUtil.drawBlock(pos, fill, outLine, lineWidth.getValue(), renderMode.getValue());
                }
            });
        }
    }

    public boolean IsHole(BlockPos blockPos) {

//        return CombatUtil.isBlockValid(blockPos);
        return HoleUtil.is2x1(blockPos) || HoleUtil.is2x2(blockPos) || HoleUtil.is1x1(blockPos)[0];
    }

    public BlockPos getClosestTargetPos() {
        if (closestTarget != null) {
            return new BlockPos(Math.floor(closestTarget.posX), Math.floor(closestTarget.posY), Math.floor(closestTarget.posZ));
        }
        return null;
    }

    public boolean isInRange(BlockPos blockPos) {
        NonNullList<Object> positions = NonNullList.create();
        positions.addAll(BlockUtil.getSphere(getPlayerPos(), range.getValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::IsHole).collect(Collectors.toList()));
        return positions.contains(blockPos);
    }

    public List<BlockPos> findCrystalBlocks() {
        NonNullList positions = NonNullList.create();
        if (smart.getValue() && closestTarget != null) {
            positions.addAll(BlockUtil.getSphere(getClosestTargetPos(), smartRange.getValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::IsHole).filter(this::isInRange).collect(Collectors.toList()));
        } else if (!smart.getValue()) {
            positions.addAll(BlockUtil.getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::IsHole).collect(Collectors.toList()));
        }
        return (List<BlockPos>) positions;
    }

    @Override
    public double getRange() {
        return range.getValue();
    }

    @Override
    public int getSafeHoles() {
        // TODO: this isn't perfect yet since we basically want to sort holes
        //  towards a player when using Smart Mode. Maybe an extra HoleManager?
        return 20;
    }

    @Override
    public int getUnsafeHoles() {
        return 20;
    }

    @Override
    public int get2x1Holes() {
        return 4;
    }

    @Override
    public int get2x2Holes() {
        return 4;
    }

//    @Override
//    public void setSafe(List<BlockPos> safe)
//    {
//        if (debug.getValue()) Command.sendMessage(safe.toString());
//        this.safes = safe;
//    }
//
//    @Override
//    public void setUnsafe(List<BlockPos> unsafe)
//    {
//        this.unsafes = unsafe;
//    }
//
//    @Override
//    public void setLongHoles(List<BlockPos> longHoles)
//    {
//        this.longs = longHoles;
//    }
//
//    @Override
//    public void setBigHoles(List<BlockPos> bigHoles)
//    {
//        this.bigs = bigHoles;
//    }
//    @Override
//    public void setFinished() {
//        /*Ignore*/
//    }
}