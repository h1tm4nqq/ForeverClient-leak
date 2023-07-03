package we.devs.forever.client.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.holefill.HoleFill;
import we.devs.forever.client.setting.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PistonPush extends Module {
    public static Setting<Double> delay = new Setting<>("Delay", 0D, 0D, 20D, "Delay for PistonPush");
    public static Setting<Boolean> enableHoleFill = new Setting<>("HoleFill", false, "Enables module\"HoleFill\" after PistonPush");
    public static Setting<Integer> time = new Setting<>("Time", 200, 0, 5000, "Time(in MS) HoleFill will disable.\nIf \"Time\" eqauls 0 HoleFill won't automatilly disable", v -> enableHoleFill.getValue());
    public static Setting<AutoSwitch> switch0 = new Setting<>("SwitchMode", AutoSwitch.Normal, "Mode of switches for PistonPush");
    private final SwitchUtil switchUtil = new SwitchUtil(switch0);
    private Stage stage = Stage.Fiend;
    private final TimerUtil timerUtil = new TimerUtil();
    private EntityPlayer target;
    private BlockPos pistonPos;
    private BlockPos redStonePos;
    private BlockPos rotatePos;
    public static PistonPush pistonPush;

    public PistonPush() {
        super("PistonPush", "Push out from hole.", Category.COMBAT);
        pistonPush = this;
    }

    @Override
    public void onEnable() {
        stage = Stage.Fiend;

    }

    @Override
    public void onDisable() {
        if (enableHoleFill.getValue()) {
            timerUtil.reset();
            HoleFill.getInstance().enable();
            Thread thread = new Thread(() -> {
                while (true) {
                    if (time.getValue() == 0) break;
                    if (timerUtil.passedMs(time.getValue())) {
                        HoleFill.getInstance().disable();
                        break;
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    @EventListener
    public void onMotionPre(MotionEvent.Pre event) {
        switch (stage) {
            case Fiend: {
                target = PlayerUtil.getNearestPlayer(6);
                if (target == null) {
                    disable("Can't find target");
                    return;
                }
                BlockPos targetPos = new BlockPos(target);
                Structure[] structures = new Structure[]{
                        new Structure(targetPos,
                                new BlockPos(1, 0, 0),//piston
                                new BlockPos(0, 0, 0),//head
                                new BlockPos[]{
                                        new BlockPos(-1, 0, 0),
                                        new BlockPos(-1, 1, 0)
                                },
                                new BlockPos(2, 0, 0),//redstone
                                new BlockPos(1, 0, 1),//redstone
                                new BlockPos(1, 0, -1),//redstone
                                new BlockPos(1, -1, 0),//redstone
                                new BlockPos(1, 1, 0)//redstone
                        ),
                        new Structure(targetPos,
                                new BlockPos(-1, 0, 0),//piston
                                new BlockPos(0, 0, 0),//head
                                new BlockPos[]{
                                        new BlockPos(1, 0, 0),
                                        new BlockPos(1, 1, 0)
                                },
                                new BlockPos(-2, 0, 0),//redstone
                                new BlockPos(-1, 0, 1),//redstone
                                new BlockPos(-1, 0, -1),//redstone
                                new BlockPos(-1, -1, 0),//redstone
                                new BlockPos(-1, 1, 0)//redstone
                        ),
                        new Structure(targetPos,
                                new BlockPos(0, 0, 1),//piston
                                new BlockPos(0, 0, 0),//head
                                new BlockPos[]{
                                        new BlockPos(0, 0, -1),
                                        new BlockPos(0, 1, -1)
                                },
                                new BlockPos(0, 0, 2),//redstone
                                new BlockPos(-1, 0, 1),//redstone
                                new BlockPos(1, 0, 1),//redstone
                                new BlockPos(0, -1, 1),//redstone
                                new BlockPos(0, 1, 1)//redstone
                        ),
                        new Structure(targetPos,
                                new BlockPos(0, 0, -1),//piston
                                new BlockPos(0, 0, 0),//head
                                new BlockPos[]{
                                        new BlockPos(0, 0, 1),
                                        new BlockPos(0, 1, 1)
                                },
                                new BlockPos(0, 0, -2),//redstone
                                new BlockPos(-1, 0, -1),//redstone
                                new BlockPos(1, 0, -1),//redstone
                                new BlockPos(0, -1, -1),//redstone
                                new BlockPos(0, 1, -1)//redstone
                        )

                };
                List<Structure> structures1 = Arrays.stream(structures).filter(Structure::isNormal).collect(Collectors.toList());
                if (structures1.isEmpty()) {
                    disable("Can't find space");
                    return;
                } else {
                    Structure structure = structures1.stream().findFirst().get();
                    pistonPos = structure.getPistonPos();
                    redStonePos = structure.getRedStonePos();
                    rotatePos = structure.getHeadPos();
                }
                stage = Stage.Piston;
                break;
            }
            case Piston: {
                int slot = InventoryUtil.findHotbarBlock(Blocks.PISTON);
                if (slot == -1) {
                    slot = InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON);
                    if (slot == -1) {
                        disable("Can't find Piston/StickyPiston");
                        return;
                    }
                }
                switchUtil.switchTo(slot);
                final float[] angle = MathUtil.calcAngle(new Vec3d(rotatePos), new Vec3d(pistonPos));
                // rotationManager.updateRotations(angle[0], angle[1]);
//                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], angle[1], mc.player.onGround));
                rotationManager.doRotation(RotationType.Legit, pistonPos,12);
                rotationManager.lookAt(angle[0], angle[1], false, true);
//                BlockUtil.placeBlock(pistonPos, false, true, true, true);
                interactionManager.placeBlock(pistonPos, RotationType.Off, true, false);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                switchUtil.switchBack();
                timerUtil.reset();
                stage = Stage.RedStone;
                break;
            }
            case RedStone: {
                if (timerUtil.passedMs(delay.getValue())) {
                    int slot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
                    if (slot == -1) {
                        slot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_TORCH);
                        if (slot == -1) {
                            disable("Can't find RedStoneBlock/RedStoneTorch");
                            return;
                        }
                    }
                    switchUtil.switchTo(slot);
//                    BlockUtil.placeBlockNotRetarded(redStonePos, EnumHand.MAIN_HAND, true, false);
                    interactionManager.placeBlock(redStonePos, RotationType.Adaptive, true, false);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    switchUtil.switchBack();
                    disable();
                }
                break;
            }
        }


    }

    public enum Stage {
        Fiend,
        Piston,
        RedStone
    }

    public static class Structure {
        private BlockPos headPos;
        private BlockPos targetPos;
        private BlockPos pistonPos;
        private BlockPos redStonePos;

        public BlockPos getHeadPos() {
            return headPos;
        }

        public BlockPos getTargetPos() {
            return targetPos;
        }

        public BlockPos getPistonPos() {
            return pistonPos;
        }

        public BlockPos getRedStonePos() {
            return redStonePos;
        }

        public Structure(BlockPos targetPos, BlockPos pistonOffset, BlockPos headOffset, BlockPos[] mostAir, BlockPos... redStoneOffset) {
            try {
                this.targetPos = targetPos;
                for (BlockPos pos : mostAir) {
                    if (!mc.world.isAirBlock(targetPos.add(pos.getX(), pos.getY() + 1, pos.getZ()))) {
                        return;
                    }
                }
                this.headPos = mc.world.isAirBlock(targetPos.add(headOffset.getX(), headOffset.getY() + 1, headOffset.getZ())) ? targetPos.add(headOffset.getX(), headOffset.getY() + 1, headOffset.getZ()) : null;
                this.pistonPos = doChecksCrystal(targetPos.add(pistonOffset.getX(), pistonOffset.getY() + 1, pistonOffset.getZ())) ? targetPos.add(pistonOffset.getX(), pistonOffset.getY() + 1, pistonOffset.getZ()) : null;
                this.redStonePos = null;

                List<BlockPos> tempRed = Arrays.stream(redStoneOffset).map(blockPos -> targetPos.add(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ())).collect(Collectors.toList());
                for (BlockPos pos : tempRed) {
                    if (doChecksCrystal(pos)) {
                        this.redStonePos = pos;
                        break;
                    }
                }
                // System.out.println(this);
            } catch (Throwable t) {
                headPos = null;
                pistonPos = null;
                this.targetPos = null;
                redStonePos = null;
            }

        }

        @Override
        public String toString() {
            return "Structure{" +
                    "headPos=" + headPos +
                    ", targetPos=" + targetPos +
                    ", pistonPos=" + pistonPos +
                    ", redStonePos=" + redStonePos +
                    '}';
        }

        public boolean isNormal() {
            return pistonPos != null && headPos != null && targetPos != null && redStonePos != null;
        }

        private boolean doChecksCrystal(BlockPos pos) {
            if (pos == null) return false;
            return BlockUtil.isPositionPlaceable(pos, false, false) == 3
                    && mc.world.isAirBlock(pos)// ||BlockUtil.getBlock() ==
                    && doChecksCrystal0(pos);
        }



        private boolean doChecksCrystal0(BlockPos pos) {
            for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity instanceof EntityExpBottle)
                    continue;
                return false;
            }
            return true;
        }
    }

}
