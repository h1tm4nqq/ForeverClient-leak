package we.devs.forever.client.modules.impl.combat;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.network.WorldClientEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.NCPUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.Page;
import we.devs.forever.client.modules.api.Timing;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Surround extends Module {
    public Setting<Page> page = new Setting<>("Page", Page.Place);
    private final Setting<Timing> timing =
            (new Setting<>("Timing", Timing.Vanilla, v -> page.getValue().equals(Page.Place)));
    private final Setting<Integer> tickDelay =
            (new Setting<>("TickDelay", 50, 0, 250, "Delay for places", v -> page.getValue().equals(Page.Place)));
    private final Setting<Integer> blocksPerTick =
            (new Setting<>("BPT", 2, 1, 10, "Blocks per tick", v -> page.getValue().equals(Page.Place)));
    private final Setting<RotationType> rotate =
            (new Setting<>("Rotate", RotationType.Off, "Rotations for places", v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> confirm =
            new Setting<>("Confirm", false, "Confirm rotations", v -> page.getValue().equals(Page.Place));
    private final Setting<Boolean> packet =
            (new Setting<>("Packet", true, "Packet places", v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> strict =
            (new Setting<>("Strict", true, "Strict interaction", v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> noLag =
            (new Setting<>("NoLag", true, "Prevent lags", v -> page.getValue().equals(Page.Place)));
    private final Setting<Integer> noLagTime =
            (new Setting<>("NoLagTicks", 1, 1, 20, "Lag Ticks", v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> clean =
            (new Setting<>("Clean", true, "Breaks unneeded crystals", v -> page.getValue().equals(Page.Place)));
    private final Setting<Boolean> jumpDisable =
            (new Setting<>("JumpDisable", true, "Disable surround if", v -> page.getValue().equals(Page.Place)));
    private final Setting<AutoSwitch> swap =
            (new Setting<>("Swap", AutoSwitch.Silent, v -> page.getValue().equals(Page.Place)));
    private final Setting<Center> center =
            (new Setting<>("Center", Center.NONE, v -> page.getValue().equals(Page.Place)));

    private final Setting<Boolean> render = (new Setting<>("Render", true, v -> page.getValue().equals(Page.Render)));
    public final Setting<Integer> duration = (new Setting<>("Duration", 1000, 1, 1000, v -> render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<RenderMode> renderMode = (new Setting<>("RenderMode", RenderMode.Fill, v -> render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Color> fillColor = (new Setting<>("Color", new Color(255, 255, 255, 100), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.Fill || renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Color> outLineColor = (new Setting<>("OutLineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline) && render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 5F, 0.1F, 10F, v -> (renderMode.getValue() == RenderMode.FillOutline || renderMode.getValue() == RenderMode.Outline || renderMode.getValue() == RenderMode.Wireframe) && render.getValue() && page.getValue().equals(Page.Render)));
    private final Setting<Boolean> debug =
            (new Setting<>("Debug", true, "Disable surround if", v -> page.getValue().equals(Page.Place)));


    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final List<BlockPos> activeBlocks = new ArrayList<>();

    private final TimerUtil renderTimer = new TimerUtil();
    private final TimerUtil lagTimer = new TimerUtil();
    private final TimerUtil delayTimer = new TimerUtil();
    private final TimerUtil hitTimer = new TimerUtil();
    private final NCPUtil ncpUtil = new NCPUtil();
    BlockPos startPos;
    SwitchUtil switchUtil = new SwitchUtil(swap);
    private boolean isSneaking;
    int blocksInTick = 0;
    int factorTicks = 0;
    private EntityPlayer interceptedBy;

    public Surround() {
        super("Surround", "Surrounds your feet with obby.", Category.COMBAT);
    }


    private enum Swap {
        PACKET,
        NORMAL
    }

    private enum Center {
        NONE,
        MOTION,
        TELEPORT
    }

    @Override
    public void onEnable() {
        if (fullNullCheck() || nullCheck()) {
            return;
        }


        this.startPos = new BlockPos(new Vec3d(MathUtil.round(mc.player.getPositionVector().x, 0), mc.player.getPositionVector().y, MathUtil.round(mc.player.getPositionVector().z, 0)));

        interceptedBy = null;

        if (center.getValue() != Center.NONE) {

            double centerX = Math.floor(mc.player.posX) + 0.5;
            double centerZ = Math.floor(mc.player.posZ) + 0.5;

            switch (center.getValue()) {
                case NONE:
                default:
                    break;
                case MOTION:
                    mc.player.motionX = (centerX - mc.player.posX) / 2;
                    mc.player.motionZ = (centerZ - mc.player.posZ) / 2;
                    break;
                case TELEPORT:
                    mc.player.setPosition(centerX, mc.player.posY, centerZ);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(centerX, mc.player.posY, centerZ, mc.player.onGround));
                    break;
            }
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        blocksInTick = 0;
    }

    @EventListener
    public void onUpdate(MotionEvent.Pre event) {
        if (!fullNullCheck() && strict.getValue() && !ncpUtil.passedInTicks(noLagTime.getValue())) {
            doFeetPlace();
        }
    }

    @Override
    public void onTick() {

        if (!fullNullCheck() && !strict.getValue()) {
            doFeetPlace();
        }
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
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck() || nullCheck()) {
            return;
        }


        if (!ncpUtil.passed(noLagTime.getValue() * 5) && !noLag.getValue()) return;

        if (event.getPacket() instanceof SPacketBlockChange) {

            if (renderBlocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                renderTimer.reset();
            }

            if (timing.getValue() == Timing.Sequential) {

                BlockPos changePos = ((SPacketBlockChange) event.getPacket()).getBlockPosition();

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) {

                    if (mc.world.getEntitiesWithinAABB(EntityPlayerSP.class, new AxisAlignedBB(changePos)).isEmpty()) {

                        if (getOffsets().contains(changePos)) {
                            activeBlocks.clear();

                            activeBlocks.add(changePos);
                            renderBlocks.put(changePos, System.currentTimeMillis());
                            placeBlock(changePos);
//                            Command.sendMessage("I place on SPacketBlockChange");
                        }
                    }
                }
            }

        }

        if (event.getPacket() instanceof SPacketMultiBlockChange) {

            if (timing.getValue() == Timing.Sequential) {


                for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks()) {

                    BlockPos changePos = blockUpdateData.getPos();

                    if (blockUpdateData.getBlockState().getMaterial().isReplaceable()) {
                        if (getOffsets().contains(changePos)) {
                            activeBlocks.clear();

                            int blockSlot = getSlot();

                            activeBlocks.add(changePos);
                            renderBlocks.put(changePos, System.currentTimeMillis());
                            placeBlock(changePos);
//                            Command.sendMessage("I place on SPacketMultiBlockChange");
                        }
                    }
                }
            }
        }

        if (event.getPacket() instanceof SPacketExplosion && !ncpUtil.passedInTicks(noLagTime.getValue())) {
//            SPacketExplosion packetExplosion = event.getPacket();
//            packetExplosion.getAffectedBlockPositions().stream().filter(this::isSurrounded)
//                doFeetPlace();
        }


        if (event.getPacket() instanceof SPacketSoundEffect && timing.getValue() == Timing.Sequential && clean.getValue() && !ncpUtil.passedInTicks(5)) {
            SPacketSoundEffect packet = event.getPacket();
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                List<BlockPos> offsets = getOffsets();

                for (BlockPos pos : offsets) {

                    for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {

                        if (offsets.contains(crystal.getPosition())) {
                            crystal.setDead();
                            interactionManager.attackCrystals(crystal.getPosition(), rotate.getValue(), strict.getValue(), Swing.Mainhand);

                            if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                                activeBlocks.clear();

                                activeBlocks.add(pos);
                                renderBlocks.put(pos, System.currentTimeMillis());
                                placeBlock(pos);
                                    Command.sendMessage("I place on SPacketSoundEffect");
                            }
                        }
                    }
                }
            }
        }

//            if (event.getPacket() instanceof SPacketSpawnObject && timing.getValue() == Timing.Sequential && clean.getValue() && ncpUtil.passedInTicks(5)) {
//                SPacketSpawnObject packet = event.getPacket();
//
//                List<BlockPos> offsets = getOffsets();
//
//                for (BlockPos pos : offsets) {
//
//                    for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {
//
//                        if (packet.getEntityID() == crystal.getEntityId()) {
//
//                            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
//                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
//                            hitTimer.reset();
//
//                            if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
//                                activeBlocks.clear();
//
//                                activeBlocks.add(pos);
//                                renderBlocks.put(pos, System.currentTimeMillis());
//                                placeBlock(pos);
////                                Command.sendMessage("I place on SPacketSpawnObject");
//                            }
//                            break;
//                        }
//                }
//            }
//        }
    }

    @EventListener
    public void onWorldLoad(WorldClientEvent.Load event) {
        disable();
    }

    private void doFeetPlace() {
        if (fullNullCheck()) return;

        if (ncpUtil.passedInTicks(noLagTime.getValue()) && noLag.getValue()) return;

        if (mc.player.motionY > 0 && jumpDisable.getValue()) {
            disable();
            return;
        }

        if (jumpDisable.getValue() && mc.player.getPositionVector().y != (double) this.startPos.getY()) {
            disable();
            return;
        }


        interceptedBy = null;

        blocksInTick = 0;

        if (delayTimer.passedMs(tickDelay.getValue())) {
            activeBlocks.clear();

            int oldSlot = mc.player.inventory.currentItem;
            int blockSlot = getSlot();

            if (blockSlot == -1) {
                disable();
                return;
            }
            isSneaking = EntityUtil.stopSneaking(isSneaking);

            for (int i = 0; i < 1; ++i) {
                List<BlockPos> offsets = getOffsets();

                for (BlockPos pos : offsets) {
                    if (blocksInTick > blocksPerTick.getValue()) {
                        continue;
                    }
                    if (isPlaceable(pos)) {
                        continue;
                    }
                    activeBlocks.add(pos);

                    boolean intercepted = false;

                    for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                        if (entity instanceof EntityEnderCrystal && clean.getValue() && hitTimer.passedMs(150)) {

                            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                            hitTimer.reset();
                            break;
                        }

                        if (entity instanceof EntityPlayer && entity != mc.player) {
                            interceptedBy = (EntityPlayer) entity;
                            intercepted = true;
                        }
                    }

                    if (intercepted) continue;

                    renderBlocks.put(pos, System.currentTimeMillis());
                    placeBlock(pos);
                }

                if (interceptedBy != null) {
                    List<BlockPos> enemyOffsets = getEnemyOffsets(interceptedBy);
                    int maxStep = enemyOffsets.size();
                    int offsetStep = 0;

                    while (blocksInTick <= blocksPerTick.getValue()) {

                        if (offsetStep >= maxStep) {
                            break;
                        }

                        BlockPos newPos = enemyOffsets.get(offsetStep++);

                        boolean foundSomeone = false;
                        for (Object entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(newPos))) {
                            if (entity instanceof EntityPlayer) {
                                foundSomeone = true;
                                break;
                            }
                        }

                        if (foundSomeone || !mc.world.getBlockState(newPos).getMaterial().isReplaceable() || isPlaceable(newPos)) {
                            continue;
                        }

                        activeBlocks.add(newPos);

                        boolean interceptedByCrystal = false;

                        for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(newPos))) {

                            interceptedByCrystal = true;

                            if (clean.getValue()) {

                                mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                                hitTimer.reset();
                                break;
                            }
                        }

                        if (interceptedByCrystal) continue;

                        renderBlocks.put(newPos, System.currentTimeMillis());
                        placeBlock(newPos);
                    }
                }

            }
            delayTimer.reset();
        }
    }

//    private void placeBlock(BlockPos pos) {
//        placeBlock(pos, clean.getValue());
//    }

    private void placeBlock(BlockPos pos) {
        if (blocksInTick > blocksPerTick.getValue()) {
            blocksInTick = 0;
            return;
        }

        if (ncpUtil.passedInTicks(noLagTime.getValue()) && noLag.getValue()) return;

        int blockSlot = getSlot();
        if (BlockUtil.checkForEntities(pos) || blockSlot == -1 || pos == null) {
            return;
        }

        if (switchUtil.switchTo(Blocks.OBSIDIAN, Blocks.ENDER_CHEST))
            placeManager.place(pos, rotate.getValue(), Swing.Mainhand, packet.getValue(), strict.getValue(), confirm.getValue(), clean.getValue());
//        interactionManager.placeBlock(pos, rotate.getValue(), packet.getValue(), strict.getValue(), boost.getValue(), confirm.getValue(), clean.getValue());

        switchUtil.switchBack();
        blocksInTick++;
    }

    private int getSlot() {
        int slot = -1;
        slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));

        if (slot == -1) {
            slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.ENDER_CHEST));
        }

        return slot;
    }

    private int getHotbarItemSlot(Item item) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                continue;
            }
            slot = i;
            break;
        }
        return slot;
    }

    private boolean isInterceptedByOther(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {

            if (entity instanceof EntityOtherPlayerMP || entity instanceof EntityItem || entity instanceof EntityEnderCrystal || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow) {
                continue;
            }

            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlaceable(BlockPos pos) {
        boolean placeable = mc.world.getBlockState(pos).getMaterial().isReplaceable();

        if (isInterceptedByOther(pos)) {
            placeable = false;
        }
        return !placeable;
    }

    private List<BlockPos> getOffsets() {
        double calcPosX = mc.player.posX;
        double calcPosZ = mc.player.posZ;

        BlockPos playerPos = getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<>();
        int z;
        int x;
        double decimalX = Math.abs(calcPosX) - Math.floor(Math.abs(calcPosX));
        double decimalZ = Math.abs(calcPosZ) - Math.floor(Math.abs(calcPosZ));
        int lengthXPos = calcLength(decimalX, false);
        int lengthXNeg = calcLength(decimalX, true);
        int lengthZPos = calcLength(decimalZ, false);
        int lengthZNeg = calcLength(decimalZ, true);
        ArrayList<BlockPos> tempOffsets = new ArrayList<>();
        offsets.addAll(getOverlapPos());
        for (x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
        }
        for (BlockPos pos2 : tempOffsets) {
            if (!isSurrounded(pos2)) {
                offsets.add(pos2.add(0, -1, 0));
            }
            offsets.add(pos2);
        }
        return offsets;
    }

    private List<BlockPos> getOverlapPos() {
        double calcPosX = mc.player.posX;
        double calcPosZ = mc.player.posZ;

        ArrayList<BlockPos> positions = new ArrayList<>();
        double decimalX = calcPosX - Math.floor(calcPosX);
        double decimalZ = calcPosZ - Math.floor(calcPosZ);
        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }

    private BlockPos getPlayerPos() {
        double calcPosX = mc.player.posX;
        double calcPosY = mc.player.posY;
        double calcPosZ = mc.player.posZ;

        double decimalPoint = calcPosY - Math.floor(calcPosY);
        return new BlockPos(calcPosX, decimalPoint > 0.8 ? Math.floor(calcPosY) + 1.0 : Math.floor(calcPosY), calcPosZ);
    }

    private List<BlockPos> getEnemyOffsets(EntityPlayer e) {
        if (e == mc.player) {
            return null;
        }
        BlockPos playerPos = getEnemyPos(e);
        ArrayList<BlockPos> offsets = new ArrayList<>();

        int z;
        int x;

        double decimalX = Math.abs(e.posX) - Math.floor(Math.abs(e.posX));
        double decimalZ = Math.abs(e.posZ) - Math.floor(Math.abs(e.posZ));

        int lengthXPos = calcLength(decimalX, false);
        int lengthXNeg = calcLength(decimalX, true);
        int lengthZPos = calcLength(decimalZ, false);
        int lengthZNeg = calcLength(decimalZ, true);

        ArrayList<BlockPos> tempOffsets = new ArrayList<>();

        offsets.addAll(getEnemyOverlapPos(e));

        for (x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
        }

        offsets.addAll(tempOffsets);

        return offsets;
    }

    private List<BlockPos> getEnemyOverlapPos(EntityPlayer e) {
        ArrayList<BlockPos> positions = new ArrayList<>();

        double decimalX = e.posX - Math.floor(e.posX);
        double decimalZ = e.posZ - Math.floor(e.posZ);

        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getEnemyPos(e));
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(getEnemyPos(e).add(properX, -1, properZ));
            }
        }
        return positions;
    }

    private BlockPos getEnemyPos(EntityPlayer e) {
        double decimalPoint = mc.player.posY - Math.floor(mc.player.posY);
        return new BlockPos(e.posX, decimalPoint > 0.8 ? Math.floor(mc.player.posY) + 1.0 : Math.floor(mc.player.posY), e.posZ);
    }

    private boolean isSurrounded(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (mc.world.getBlockState(pos.offset(facing)).getBlock() == Blocks.AIR) {
                continue;
            }
            return true;
        }
        return false;
    }

    private BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(x, y, z);
    }

    private int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
    }

    private int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }
}