

package we.devs.forever.client.modules.impl.combat;


import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.CombatUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.DirectionMode;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class AutoPiston extends Module {
    //public Setting<Float> preDelay = new Setting<>("BlockDelay", 0.0f, 0.0f, 25.0f);
    public Setting<Float> globalDelay = new Setting<>("GlobalDelay", 0.0f, 0.0f, 25.0f);
    public Setting<Float> pistonDelay = new Setting<>("PistonDelay", 0.0f, 0.0f, 25.0f);
    public Setting<Float> crystalDelay = new Setting<>("CrystalDelay", 0.0f, 0.0f, 25.0f);
    public Setting<Float> redstoneDelay = new Setting<>("RedStoneDelay", 0.0f, 0.0f, 25.0f);
    public Setting<Float> pushDelay = new Setting<>("PushDelay", 0.0f, 0.0f, 25.0f);
    public Setting<patterns> patternsSetting = new Setting<>("Pattern", patterns.All);
    public Setting<Boolean> bypass = new Setting<>("FireBypass", false, "Breaks crystals by fire,very speed up AutoPiston.\nItem request - Flint and Steel");
    public Setting<Boolean> moreDamage = new Setting<>("MoreDamage", false, "MoreDamage");
    protected Setting<DirectionMode> directionMode = (new Setting<>("Interact", DirectionMode.Normal, "Place interact for place crystal."));
    protected Setting<RotationType> rotate = (new Setting<>("Rotate", RotationType.Off, "Rotations for places."));
    protected Setting<Boolean> strictDirection = (new Setting<>("StrictDirection", false, "StrictDirection."));
    protected Setting<Boolean> packetPlace = (new Setting<>("PacketPlace", false, "PacketPlace."));
    protected Setting<Boolean> supportPlace = (new Setting<>("SupportPlace", false, "Place support blocks for stuff."));
    protected Setting<Boolean> checkBurrow = (new Setting<>("CheckBurrow", false, "Disable module if target burrowed."));
    protected Setting<Boolean> boostPlace = (new Setting<>("BoostPlace", false, "Boosted places."));
    public Setting<Boolean> trap = new Setting<>("Trap", false);
    public Setting<Float> trapDelay = new Setting<>("TrapDelay", 3.0f, 0.0f, 25.0f, s -> trap.getValue());
    public Setting<Float> targetRange = new Setting<>("Target Range", 10.0f, 0.0f, 20.0f);
    public Setting<Float> range = new Setting<>("Range", 10.0f, 1.0f, 20.0f);
    public Setting<AutoSwitch> switchh = new Setting<>("Switch", AutoSwitch.Normal);
    public Setting<Boolean> checkEntity = new Setting<>("CheckEntity", false);
    public Setting<Integer> maxY = new Setting<>("MaxY", 2, 1, 5);
    protected Setting<Boolean> debug = (new Setting<>("Debug", false, "Debug"));
    SwitchUtil switchUtil = new SwitchUtil(switchh);
    public List<BlockPos> debugPosess = new ArrayList<>();
    public int oldslot = -1;
    public EnumHand oldhand = null;
    public EntityPlayer target;
    public BlockPos pistonPos;
    public BlockPos crystalPos;
    public BlockPos redStonePos;
    public boolean placedPiston;
    public boolean placedCrystal;
    public boolean placedRedStone;
    public boolean waitedPiston;
    public boolean brokeCrystal;
    public boolean builtTrap;
    public boolean done;
    public boolean isFire;
    public boolean retrying;
    public boolean digging;
    public TimerUtil pistonTimer = new TimerUtil();
    public TimerUtil crystalTimer = new TimerUtil();
    public TimerUtil redStoneTimer = new TimerUtil();
    public TimerUtil pistonCrystalTimer = new TimerUtil();
    public TimerUtil breakTimer = new TimerUtil();
    public TimerUtil preTimer = new TimerUtil();
    public TimerUtil trapTimer = new TimerUtil();
    public TimerUtil syncTimer = new TimerUtil();
    public TimerUtil mainDelay = new TimerUtil();
    public int pistonSlot;
    public int crystalSlot;
    public int redstoneSlot;
    public int obbySlot;
    public int pickelSlot;
    public int trapTicks = 0;
    public Stage stage = Stage.Fiend;
    public int attempts = 0;
    public BlockPos oldPiston;
    public BlockPos oldRedstone = null;
    public int tmpSlot = 0;
    BlockPos targetPos;
    BlockPos firePos;
    BlockPos rotatePos;
    float pitch, yaw;
    Entity crystal;
    protected Vec3d rotationVector;
    protected RayTraceResult postResult;
    TimerUtil rotationTimer = new TimerUtil();
    public static AutoPiston autoPiston;

    public enum patterns {
        Small,
        Cross,
        Liner,
        All
    }

    public AutoPiston() {
        super("AutoPiston",
                "Pistons targets. Items need:\n" +
                        "Flint(Not compulsory,need for bypass mechanic Minecraft,\n" +
                        "breaks crystal by fire, speed up AutoPiston),\n" +
                        "RedStoneBlock/RedStoneTorch, Piston/StickyPiston, EnderCrystal.", Category.COMBAT);
        autoPiston = this;
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onLogout() {
        reset();
        disable();
    }

    @Override
    public void onLoad() throws Throwable {
        reset();
        disable();
    }

    @Override
    public void onUnload() throws Throwable {
        reset();
        disable();
    }

    @EventListener
    public void onMotionPre(MotionEvent.Pre event) {
        if (mainDelay.passedMs(globalDelay.getValue()) || globalDelay.getValue() == 0) doPA();
    }

    public void doPA() {
        if (nullCheck()) {
            return;
        }
        try {
            switch (stage) {
                case Fiend: {
                    findPos(true);
                    stage = Stage.Trap;
                    break;
                }
                case Trap: {
                    if (!trap.getValue()) {
                        stage = Stage.Piston;
                        break;
                    }
                    if (BlockUtil.getBlock(targetPos.add(0, 2, 0)) == Blocks.OBSIDIAN || pistonPos.getY() >= targetPos.add(0, 2, 0).getY()) {
                        stage = Stage.Piston;
                        break;
                    }

                    if (!builtTrap && trapTimer.passedMs(trapDelay.getValue())) {
                        final BlockPos offset = new BlockPos(crystalPos.getX() - targetPos.getX(), 0, crystalPos.getZ() - targetPos.getZ());
                        final BlockPos trapBase = targetPos.add(offset.getX() * -1, 0, offset.getZ() * -1);
                        if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1) {
                            disable("Can't fiend Obsidian");
                            return;
                        } else {
                            switchUtil.switchTo(Blocks.OBSIDIAN);
                        }
                        if (tmpSlot == 0) {
                            placeBlock(trapBase.add(0, 1, 0), false);
//                            BlockUtil.placeBlock(trapBase.add(0, 2, 0), true, strictDirection.getValue(), packetPlace.getValue(), false);
                            tmpSlot++;
                            return;
                        } else if (tmpSlot == 1) {
                            placeBlock(trapBase.add(0, 2, 0), false);
//                            BlockUtil.placeBlock(trapBase.add(0, 2, 0), true, strictDirection.getValue(), packetPlace.getValue(), false);
                            tmpSlot++;
                            return;
                        } else if (tmpSlot == 2) {
                            placeBlock(targetPos.add(0, 2, 0), false);
//                            BlockUtil.placeBlock(trapBase.add(0, 2, 0), true, strictDirection.getValue(), packetPlace.getValue(), false);
                            tmpSlot = 0;
                            return;
                        }
                        builtTrap = true;
                        trapTimer.reset();
                        stage = Stage.Piston;
                        break;
                    }
                    mainDelay.reset();
                    break;
                }
                case Piston: {
//                    if (mc.player.getDistanceSq(target) >= targetRange.getValue() || !CombatUtil.isInHole(target))
//                        disable("Target so far or is not in hole");
//                    rotationManager.updateRotations(angle[0], angle[1]); //Just test
                    if (!BlockUtil.canPlace(pistonPos, false)) {
                        stage = !moreDamage.getValue() ? Stage.Fire : Stage.Crystal;
                        break;
                    }
                    if (!placedPiston) {
                        BlockPos support = pistonPos.down();
                        if (BlockUtil.empty.contains(mc.world.getBlockState(support).getBlock()) && supportPlace.getValue()) {
                            switchUtil.switchTo(Blocks.OBSIDIAN);
                            interactionManager.placeBlock(support, rotate.getValue(), packetPlace.getValue(), strictDirection.getValue(), boostPlace.getValue(), false);
                            switchUtil.switchBack();
                        }
                        if (InventoryUtil.findHotbarBlock(Blocks.PISTON) == -1) {
                            if (InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON) == -1) {
                                disable("Can't find Piston/StickyPiston");
                                return;
                            } else {
                                switchUtil.switchTo(InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON));
                            }
                        } else {
                            switchUtil.switchTo(InventoryUtil.findHotbarBlock(Blocks.PISTON));
                        }

                        final float[] angle = MathUtil.calcAngle(new Vec3d(rotatePos), new Vec3d(pistonPos));
                        rotationManager.doRotation(RotationType.Legit, pistonPos, 12);
                        rotationManager.lookAt(angle[0], angle[1], false, true);
                        BlockUtil.placeBlock(pistonPos, false, strictDirection.getValue(), true, boostPlace.getValue());
//                        else interactionManager.placeBlock(pistonPos, false, true, strictDirection.getValue(), boostPlace.getValue(), false);
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        switchUtil.switchBack();
                        pistonTimer.reset();
                        placedPiston = true;
                    }
                    if (pistonTimer.passedMs(pistonDelay.getValue()) && placedPiston)
                        stage = !moreDamage.getValue() ? Stage.Fire : Stage.Crystal;
//                    if (moreDamage.getValue()) crystalTimer.reset();
                    mainDelay.reset();
                    break;
                }
                case Fire: {
                    if (isFire) {
                        BlockPos support = firePos.down();
                        if (BlockUtil.empty.contains(mc.world.getBlockState(support).getBlock()) && supportPlace.getValue()) {
                            switchUtil.switchTo(Blocks.OBSIDIAN);
                            interactionManager.placeBlock(support, rotate.getValue(), packetPlace.getValue(), strictDirection.getValue(), boostPlace.getValue(), false);
                            switchUtil.switchBack();
                        }
                        switchUtil.switchTo(Items.FLINT_AND_STEEL);

                        rotationManager.doRotation(rotate.getValue(), firePos, 12);
                        BlockUtil.rightClickBlock(firePos, new Vec3d(firePos), EnumHand.MAIN_HAND, EnumFacing.UP, false);
//                        interactionManager.placeBlock(firePos, true, false, false);
                        switchUtil.switchBack();
                    }
                    stage = !moreDamage.getValue() ? Stage.Crystal : Stage.Final;
                    crystalTimer.reset();
                    mainDelay.reset();
                    break;
                }
                case Crystal: {
                    yaw = yaw + (new Random().nextInt(4) - 2) / 100f;
                    BlockPos support = crystalPos.down();
                    if (BlockUtil.empty.contains(mc.world.getBlockState(support).getBlock()) && supportPlace.getValue()) {
                        switchUtil.switchTo(Blocks.OBSIDIAN);
                        interactionManager.placeBlock(support, rotate.getValue(), packetPlace.getValue(), strictDirection.getValue(), boostPlace.getValue(), false);
                        switchUtil.switchBack();
                    }
                    EnumFacing facing = handlePlaceRotation(crystalPos);
                    if (facing == null) return;
                    rotationManager.doRotation(rotate.getValue(), yaw, pitch, 12);
//                    rotationManager.lookAt(yaw, pitch, false, true);
                    if (crystalTimer.passedMs(crystalDelay.getValue())) {
                        final EnumHand hand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                        if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                            switchUtil.switchTo(Items.END_CRYSTAL);
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(crystalPos, facing, hand, (float) postResult.hitVec.x, (float) postResult.hitVec.y, (float) postResult.hitVec.z));
                            switchUtil.switchBack();
                        } else {
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(crystalPos, facing, hand, (float) postResult.hitVec.x, (float) postResult.hitVec.y, (float) postResult.hitVec.z));
                        }
                        stage = Stage.RedStone;
                        redStoneTimer.reset();
                    }
                    mainDelay.reset();
                    break;
                }
                case RedStone: {

                    BlockPos support = redStonePos.down();
                    if (BlockUtil.empty.contains(mc.world.getBlockState(support).getBlock()) && supportPlace.getValue()) {
                        switchUtil.switchTo(Blocks.OBSIDIAN);
                        interactionManager.placeBlock(support, rotate.getValue(), packetPlace.getValue(), strictDirection.getValue(), boostPlace.getValue(), false);
                        switchUtil.switchBack();
                    }


                    if (redStoneTimer.passedMs(redstoneDelay.getValue()) && !placedRedStone) {
                        if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
                            if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_TORCH) == -1) {
                                disable("Can't find RedStoneBlock/RedStoneTorch");
                            } else {
                                switchUtil.switchTo(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_TORCH));
                            }
                        } else {
                            switchUtil.switchTo(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
                        }
//                        rotationManager.lookAt(firePos, false, 12);
//                        BlockUtil.placeBlock(redStonePos, true, strictDirection.getValue(), packetPlace.getValue(), true);
                        interactionManager.placeBlock(redStonePos, rotate.getValue(), packetPlace.getValue(), strictDirection.getValue(), boostPlace.getValue(), false);
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        switchUtil.switchBack();
                        placedRedStone = true;

                        pistonCrystalTimer.reset();

                    }

                    if (pistonCrystalTimer.passedMs(pushDelay.getValue()) && placedRedStone) {
                        stage = !moreDamage.getValue() ? Stage.Final : Stage.Fire;
                    }
                    mainDelay.reset();
                    break;
                }
                case Break: {
                    breakTimer.reset();
                    if (!isFire) {
                        List<Entity> crystal = mc.world.loadedEntityList.stream()
                                .filter(entity -> entity instanceof EntityEnderCrystal)
                                .filter(entity -> target.getDistanceSq(entity) <= 9)
                                .collect(Collectors.toList());
                        if (crystal.isEmpty()) {
                            stage = Stage.Final;
                        } else {
                            EntityUtil.attackEntity(crystal.stream().findFirst().get(), false, Swing.Mainhand);
                        }
                    } else {
                        stage = Stage.Final;
                    }
                    mainDelay.reset();
                    break;
                }
                case Final: {
                    placedPiston = false;
                    placedRedStone = false;
                    pistonTimer.reset();
                    crystalTimer.reset();
                    redStoneTimer.reset();
                    pistonCrystalTimer.reset();
                    breakTimer.reset();
                    preTimer.reset();
                    trapTimer.reset();
                    syncTimer.reset();
                    attempts = 0;
                    stage = Stage.Piston;
                    mainDelay.reset();
                    break;
                }
            }

        } catch (Exception e2) {
            e2.printStackTrace();
            disable("Caught Exception - " + e2.getMessage());
        }

        globalCheck();

        mainDelay.reset();
    }


    public void breakCrystal() {

        for (Entity entities : mc.world.loadedEntityList) {
            if (!(entities instanceof EntityEnderCrystal) || !(mc.player.getDistance(entities.posX, entities.posY, entities.posZ) < (double) range.getValue()) || EntityUtil.isSafe(mc.player))
                continue;
//            rotationManager.lookAt(entities, false, 12);
//            interactionManager.attackCrystals(entities.getPosition(), true);
            interactionManager.attackEntity(entities, true, Swing.Mainhand);
        }
    }

    void globalCheck() {
        boolean isNull = firePos == null || crystalPos == null || pistonPos == null || targetPos == null;
        if (isNull) {
            return;
        }
        boolean canPlace =!BlockUtil.canPlace(crystalPos, true)
                || !BlockUtil.canPlace(pistonPos, true)
                || !BlockUtil.canPlace(redStonePos, true);
        if (canPlace && stage == Stage.Fiend)
            breakCrystal();
        canPlace = !BlockUtil.canPlace(crystalPos, false)
                || !BlockUtil.canPlace(pistonPos, false)
                || !BlockUtil.canPlace(redStonePos, false)
                || !BlockUtil.canPlace(firePos, false);
        if (canPlace) {
            if (debug.getValue()) Command.sendMessage("We can't place stuff. Recalcing...");
            findPos(false);
        }

        if (BlockUtil.isBlockSolid(targetPos) && checkBurrow.getValue()) disable("Target burrowed");
    }

    @Override
    public String getDisplayInfo() {
        return stage.toString();
    }

    private void switchFromHotbar(int slot) {
        mc.player.inventory.currentItem = slot;
        InventoryUtil.syncItem();
    }


    @Override
    public void onRender3D(Render3DEvent event) {

        if (isNull(pistonPos) || isNull(crystalPos) || isNull(redStonePos)) {
            return;
        }

        BlockRenderUtil.drawBlock(crystalPos, null, Color.PINK, 4F, RenderMode.Outline);
        BlockRenderUtil.drawBlock(pistonPos, null, Color.GREEN, 4F, RenderMode.Outline);
        BlockRenderUtil.drawBlock(redStonePos, null, Color.RED, 4F, RenderMode.Outline);
        if (!isNull(firePos))
            BlockRenderUtil.drawBlock(firePos, null, Color.white, 4F, RenderMode.Outline);


    }

    private void findPos(boolean disable) {
        ArrayList<Structure> list = new ArrayList<>();
        List<EntityPlayer> possibleTargets = CombatUtil.getPlayersSorted(targetRange.getValue()).stream()
                .filter(entityPlayer -> EntityUtil.isValid(entityPlayer, targetRange.getValue()))
                .collect(Collectors.toList());
        for (EntityPlayer target : possibleTargets) {
            for (int i = 0; i <= maxY.getValue(); i++) {
                if (patternsSetting.getValue() == patterns.Small || patternsSetting.getValue() == patterns.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(1, i, -1),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(1, 0, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, 0, 1), new BlockPos(1, i, 1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(1, i, 1),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(1, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(1, i, -1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(1, i, 1),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(2, i, 1)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(-1, i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(-1, i, 1),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-2, i, 1)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 1), new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-1, i, 1),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-1, i, -1),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(0, i, 1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(-1, i, -1),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-2, i, -1)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 0), new BlockPos(1, i, -1), new BlockPos(1, i + 1, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(1, i, -1),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(2, i, -1)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(-1, i, -1), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                }
                if (patternsSetting.getValue() == patterns.Cross || patternsSetting.getValue() == patterns.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, -1),//piston
                            new BlockPos(1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, -1), new BlockPos(2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(1, i, 1), new BlockPos(0, i, 1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, 1),//piston
                            new BlockPos(1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, 1), new BlockPos(2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, 1), new BlockPos(1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, -1), new BlockPos(0, 1 + i, 1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(1, i, 2),//piston
                            new BlockPos(1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(1, i, 3), new BlockPos(2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 0), new BlockPos(-1, i, 0), new BlockPos(-1, i, 1), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(-1, i, 2),//piston
                            new BlockPos(-1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, 3), new BlockPos(-2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(1, i, 1), new BlockPos(1, i, 0), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, 1),//piston
                            new BlockPos(-1, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, 1), new BlockPos(-2, i, 2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, 1), new BlockPos(-1, i, -1), new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, -1),//piston
                            new BlockPos(-1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, -1), new BlockPos(-2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(0, i, -1), new BlockPos(0, i, -1), new BlockPos(-1, i, 1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(-1, i, -2),//piston
                            new BlockPos(-1, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(-1, i, -3), new BlockPos(-2, i, -2)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 0), new BlockPos(1, i, 0), new BlockPos(1, i, -1), new BlockPos(-1, 1 + i, 0), new BlockPos(1, 1 + i, 0)})//fire
                    );
                }
                if (patternsSetting.getValue() == patterns.Liner || patternsSetting.getValue() == patterns.All) {
                    list.add(new Structure(
                            target,
                            new BlockPos(1, i, 0),//crystal
                            new BlockPos(2, i, 0),//piston
                            new BlockPos(1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(3, i, 0)},//redstone
                            new BlockPos[]{new BlockPos(1, i, 1), new BlockPos(1, i, -1), new BlockPos(0, i, 1),
                                    new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(-1, i, 0),//crystal
                            new BlockPos(-2, i, 0),//piston
                            new BlockPos(-1, i, 0),//pistonHead
                            new BlockPos[]{new BlockPos(-3, i, 0)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(-1, i, 1), new BlockPos(0, i, 1),
                                    new BlockPos(0, i, -1), new BlockPos(0, 1 + i, 1), new BlockPos(0, 1 + i, -1)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, 1),//crystal
                            new BlockPos(0, i, 2),//piston
                            new BlockPos(0, i, 1),//pistonHead
                            new BlockPos[]{new BlockPos(0, i, 3)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, 1), new BlockPos(1, i, 1), new BlockPos(-1, i, 0),
                                    new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                    list.add(new Structure(
                            target,
                            new BlockPos(0, i, -1),//crystal
                            new BlockPos(0, i, -2),//piston
                            new BlockPos(0, i, -1),//pistonHead
                            new BlockPos[]{new BlockPos(0, i, -3)},//redstone
                            new BlockPos[]{new BlockPos(-1, i, -1), new BlockPos(1, i, -1), new BlockPos(-1, i, 0),
                                    new BlockPos(1, i, 0), new BlockPos(1, 1 + i, 0), new BlockPos(-1, 1 + i, 0)})//fire
                    );
                }
            }
            this.target = target;
        }

        if (bypass.getValue()) {
            List<Structure> structure0 = list.stream().filter(Structure::isFirePa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
            if (structure0.size() == 0) {
                isFire = false;
                structure0 = list.stream().filter(Structure::isNormalPa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
                if (structure0.size() != 0) {
                    Structure structure = structure0.get(0);
                    pistonPos = structure.getPistonPos();
                    crystalPos = structure.getCrystalPos();
                    redStonePos = structure.getRedstonePos();
                    rotatePos = structure.getRotatePos();
                    targetPos = structure.targetPos;
                    target = structure.getTarget();
                } else {
                    if (disable) disable("Can't find space.");
                }
            } else {
                isFire = true;
                Structure structure = structure0.get(0);
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedstonePos();
                firePos = structure.getFirePos();
                rotatePos = structure.getRotatePos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            }
        } else {
            isFire = false;
            List<Structure> structure0 = list.stream().filter(Structure::isNormalPa).sorted(Comparator.comparingDouble(Structure::getMaxRange)).collect(Collectors.toList());
            if (structure0.size() != 0) {
                Structure structure = structure0.get(0);
                pistonPos = structure.getPistonPos();
                crystalPos = structure.getCrystalPos();
                redStonePos = structure.getRedstonePos();
                rotatePos = structure.getRotatePos();
                targetPos = structure.targetPos;
                target = structure.getTarget();
            } else {
                if (disable) disable("Can't find space.");
            }
        }
    }

    public EnumFacing handlePlaceRotation(BlockPos pos) {
        if (pos == null || mc.player == null) {
            return null;
        }
        EnumFacing facing = null;
        if (directionMode.getValue() != DirectionMode.Vanilla) {
            Vec3d placeVec = null;
            double[] placeRotation = null;

            double increment = 0.45D;
            double start = 0.05D;
            double end = 0.95D;

            Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()), mc.player.posZ);

            for (double xS = start; xS <= end; xS += increment) {
                for (double yS = start; yS <= end; yS += increment) {
                    for (double zS = start; zS <= end; zS += increment) {
                        Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                        double distToPosVec = eyesPos.distanceTo(posVec);
                        double diffX = posVec.x - eyesPos.x;
                        double diffY = posVec.y - eyesPos.y;
                        double diffZ = posVec.z - eyesPos.z;
                        double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                        double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                        // inline values for slightly better perfornamce
                        float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                        float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                        float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                        float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                        Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                        Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, true, false);
                        if ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.getBlockPos().equals(pos))) {

                            if (strictDirection.getValue()) {
                                if (placeVec != null && placeRotation != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || facing == null)) {
                                    if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(posVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(placeVec)) {
                                        placeVec = posVec;
                                        placeRotation = tempPlaceRotation;
                                        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                            facing = rayTraceResult.sideHit;
                                            postResult = rayTraceResult;
                                        }
                                    }
                                } else {
                                    placeVec = posVec;
                                    placeRotation = tempPlaceRotation;
                                    if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                        facing = rayTraceResult.sideHit;
                                        postResult = rayTraceResult;
                                    }
                                }
                            } else {
                                if (placeVec != null && placeRotation != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || facing == null)) {
                                    if (Math.hypot((((tempPlaceRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempPlaceRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                            Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                        placeVec = posVec;
                                        placeRotation = tempPlaceRotation;
                                        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                            facing = rayTraceResult.sideHit;
                                            postResult = rayTraceResult;
                                        }
                                    }
                                } else {
                                    placeVec = posVec;
                                    placeRotation = tempPlaceRotation;
                                    if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                        facing = rayTraceResult.sideHit;
                                        postResult = rayTraceResult;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (directionMode.getValue() == DirectionMode.Strict) {
                if (placeRotation != null && facing != null) {
                    rotationTimer.reset();
                    rotationVector = placeVec;
                    float[] angle = Client.rotationManager.calculateAngle(rotationVector);
                    yaw = angle[0];
                    pitch = angle[1];
                    return facing;
                } else {
                    for (double xS = start; xS <= end; xS += increment) {
                        for (double yS = start; yS <= end; yS += increment) {
                            for (double zS = start; zS <= end; zS += increment) {
                                Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                                double distToPosVec = eyesPos.distanceTo(posVec);
                                double diffX = posVec.x - eyesPos.x;
                                double diffY = posVec.y - eyesPos.y;
                                double diffZ = posVec.z - eyesPos.z;
                                double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                                double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                                // inline values for slightly better perfornamce
                                float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                                float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                                float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                                float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                                Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                                Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, true, true);
                                if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {

                                    if (strictDirection.getValue()) {
                                        if (placeVec != null && placeRotation != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || facing == null)) {
                                            if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(posVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(placeVec)) {
                                                placeVec = posVec;
                                                placeRotation = tempPlaceRotation;
                                                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                    facing = rayTraceResult.sideHit;
                                                    postResult = rayTraceResult;
                                                }
                                            }
                                        } else {
                                            placeVec = posVec;
                                            placeRotation = tempPlaceRotation;
                                            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                facing = rayTraceResult.sideHit;
                                                postResult = rayTraceResult;
                                            }
                                        }
                                    } else {
                                        if (placeVec != null && placeRotation != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || facing == null)) {
                                            if (Math.hypot((((tempPlaceRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempPlaceRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                                    Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                                placeVec = posVec;
                                                placeRotation = tempPlaceRotation;
                                                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                    facing = rayTraceResult.sideHit;
                                                    postResult = rayTraceResult;
                                                }
                                            }
                                        } else {
                                            placeVec = posVec;
                                            placeRotation = tempPlaceRotation;
                                            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                facing = rayTraceResult.sideHit;
                                                postResult = rayTraceResult;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (placeRotation != null) {
                    rotationTimer.reset();
                    rotationVector = placeVec;
                    float[] angle = Client.rotationManager.calculateAngle(rotationVector);
                    yaw = angle[0];
                    pitch = angle[1];
                }
                if (facing != null) {
                    return facing;
                }
            }
        } else {
            EnumFacing bestFacing = null;
            Vec3d bestVector = null;
            for (EnumFacing enumFacing : EnumFacing.values()) {
                Vec3d cVector = new Vec3d(pos.getX() + 0.5 + enumFacing.getDirectionVec().getX() * 0.5,
                        pos.getY() + 0.5 + enumFacing.getDirectionVec().getY() * 0.5,
                        pos.getZ() + 0.5 + enumFacing.getDirectionVec().getZ() * 0.5);
                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), cVector, false, true, false);
                if (rayTraceResult != null && rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && rayTraceResult.getBlockPos().equals(pos)) {
                    if (strictDirection.getValue()) {
                        if (bestVector == null || mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(cVector) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(bestVector)) {
                            bestVector = cVector;
                            bestFacing = enumFacing;
                            postResult = rayTraceResult;
                        }
                    } else {
                        rotationTimer.reset();
                        rotationVector = cVector;
                        float[] angle = Client.rotationManager.calculateAngle(rotationVector);
                        yaw = angle[0];
                        pitch = angle[1];
                        return enumFacing;
                    }
                }
            }
            if (bestFacing != null) {
                rotationTimer.reset();
                rotationVector = bestVector;
                float[] angle = Client.rotationManager.calculateAngle(rotationVector);
                yaw = angle[0];
                pitch = angle[1];
                return bestFacing;
            } else if (strictDirection.getValue()) {
                for (EnumFacing enumFacing : EnumFacing.values()) {
                    Vec3d cVector = new Vec3d(pos.getX() + 0.5 + enumFacing.getDirectionVec().getX() * 0.5,
                            pos.getY() + 0.5 + enumFacing.getDirectionVec().getY() * 0.5,
                            pos.getZ() + 0.5 + enumFacing.getDirectionVec().getZ() * 0.5);
                    if (bestVector == null || mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(cVector) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(bestVector)) {
                        bestVector = cVector;
                        bestFacing = enumFacing;
                    }
                }
                if (bestFacing != null) {
                    rotationTimer.reset();
                    rotationVector = bestVector;
                    float[] angle = Client.rotationManager.calculateAngle(rotationVector);
                    yaw = angle[0];
                    pitch = angle[1];
                    return bestFacing;
                }
            }
        }
        if ((double) pos.getY() > mc.player.posY + (double) mc.player.getEyeHeight()) {
            rotationTimer.reset();
            rotationVector = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            float[] angle = Client.rotationManager.calculateAngle(rotationVector);
            yaw = angle[0];
            pitch = angle[1];
            return EnumFacing.DOWN;
        }
        rotationTimer.reset();
        rotationVector = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        float[] angle = Client.rotationManager.calculateAngle(rotationVector);
        yaw = angle[0];
        pitch = angle[1];
        return EnumFacing.UP;
    }


    public boolean isNull(final Object b) {
        return b == null;
    }


    public void placeBlock(final BlockPos pos, final Boolean packet) {
        if (BlockUtil.isPositionPlaceable(pos, false, true) != 3) return;
        BlockUtil.placeBlockNotRetarded(pos, EnumHand.MAIN_HAND, true, false);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    public void reset() {
        placedPiston = false;
        placedRedStone = false;
        target = null;
        stage = Stage.Fiend;
        pistonTimer.reset();
        crystalTimer.reset();
        redStoneTimer.reset();
        pistonCrystalTimer.reset();
        breakTimer.reset();
        preTimer.reset();
        trapTimer.reset();
        syncTimer.reset();
        mainDelay.reset();
    }

    public enum Target {
        Nearest,
        Looking,
        Best;
    }


    public enum Stage {
        Fiend,
        Trap,
        Piston,
        Fire,
        Crystal,
        RedStone,
        Break,
        Final
    }

    public class Structure {
        private BlockPos pistonPos;
        private BlockPos crystalPos;
        private BlockPos targetPos;
        private BlockPos redstonePos;
        private BlockPos firePos;
        private EntityPlayer target;

        private BlockPos rotatePos;

        public BlockPos getRotatePos() {
            return rotatePos;
        }

        public BlockPos getPistonPos() {
            return pistonPos;
        }

        public BlockPos getCrystalPos() {
            return crystalPos;
        }

        public BlockPos getRedstonePos() {
            return redstonePos;
        }

        public BlockPos getFirePos() {
            return firePos;
        }

        public EntityPlayer getTarget() {
            return target;
        }


        public Structure(EntityPlayer target, BlockPos crystalPos, BlockPos pistonPos, BlockPos pistonHeadPos, BlockPos[] redstonePos, BlockPos[] firePos) {
            try {
                this.target = target;
                this.targetPos = new BlockPos(target.posX, target.posY, target.posZ);
                this.pistonPos = doChecks(targetPos.add(pistonPos.getX(), pistonPos.getY() + 1, pistonPos.getZ())) ? targetPos.add(pistonPos.getX(), pistonPos.getY() + 1, pistonPos.getZ()) : null;
                this.crystalPos = BlockUtil.canPlaceCrystal(targetPos.add(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ())) ? targetPos.add(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ()) : null;
                rotatePos = mc.world.isAirBlock(targetPos.add(pistonHeadPos.getX(), pistonHeadPos.getY() + 1, pistonHeadPos.getZ())) ? targetPos.add(pistonHeadPos.getX(), pistonHeadPos.getY(), pistonHeadPos.getZ()) : null;

                this.redstonePos = null;
                List<BlockPos> tempRed = Arrays.stream(redstonePos).map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ())).collect(Collectors.toList());
                for (BlockPos pos : tempRed) {
                    if (doChecks(pos)) {
                        this.redstonePos = pos;
                        break;
                    }
                }
                this.firePos = null;
                List<BlockPos> tempFire = Arrays.stream(firePos).map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY(), blockPos.getZ())).collect(Collectors.toList());
                for (BlockPos pos : tempFire) {
                    if (doChecksFire(pos)) {
                        this.firePos = pos;
                        break;
                    }
                }
            } catch (Throwable t) {
                this.pistonPos = null;
                this.crystalPos = null;
                this.targetPos = null;
                this.redstonePos = null;
                this.firePos = null;
                rotatePos = null;
            }

        }

        public boolean isNormalPa() {
            return pistonPos != null && crystalPos != null && targetPos != null && redstonePos != null && rotatePos != null;
        }

        public boolean isFirePa() {
            return pistonPos != null && crystalPos != null && targetPos != null && redstonePos != null && rotatePos != null && firePos != null;
        }

        private boolean doChecks(BlockPos pos) {
            if (pos == null) return false;
            return canPlace(pos)
                    && mc.world.isAirBlock(pos)
                    && (doChecksCrystal(pos) || !checkEntity.getValue());
        }

        private boolean canPlace(BlockPos pos) {
            for (EnumFacing side : BlockUtil.getPossibleSides(pos,false)) {
                if (BlockUtil.canBeClicked(pos.offset(side))) {
                    return true;
                }
            }
            return false;
        }

        private boolean doChecksCrystal(BlockPos pos) {
            for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity instanceof EntityExpBottle)
                    continue;
                return false;
            }
            return true;
        }

        private boolean doChecksFire(BlockPos pos) {
            if (pos == null) return false;
            return BlockUtil.isPositionPlaceable(pos, false, false) < 1
                    && mc.world.isAirBlock(pos.add(0, 1, 0)) || BlockUtil.getBlock(pos.add(0, 1, 0)).equals(Blocks.FIRE);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Structure)) return false;
            Structure that = (Structure) o;
            return Objects.equals(pistonPos, that.pistonPos)
                    && Objects.equals(crystalPos, that.crystalPos)
                    && Objects.equals(redstonePos, that.redstonePos)
                    && Objects.equals(firePos, that.firePos);
        }

        @Override
        public String toString() {
            return "Structure{" +
                    "pistonPos=" + pistonPos +
                    ", crystalPos=" + crystalPos +
                    ", targetPos=" + targetPos +
                    ", redstonePos=" + redstonePos +
                    ", firePos=" + firePos +
                    '}';
        }

        public double getMaxRange() {
            final double piston = PlayerUtil.getDistance(this.pistonPos);
            final double crystal = PlayerUtil.getDistance(this.crystalPos);
            final double redstone = PlayerUtil.getDistance(this.redstonePos);
            final double fire = PlayerUtil.getDistance(this.firePos);
            return Math.max(Math.max(fire, crystal), Math.max(redstone, piston));
        }
    }
}
