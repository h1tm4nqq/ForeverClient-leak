package we.devs.forever.client.modules.impl.combat.autoMine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.combat.VulnerabilityUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.exploit.speedmine.SpeedMine;
import we.devs.forever.client.setting.Setting;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public class AutoMine extends Module {
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Forever, "Mode Forever: use client SpeedMine for break\n Mode All: click on pos for break");
    public Setting<Float> range = new Setting<>("Range", 6F, 1F, 6F);
    public Setting<Float> wallRange = new Setting<>("WallRange", 3F, 1F, 6F);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true, "Use strict bypasses for get facing");

    public Setting<Boolean> burrow = new Setting<>("BreakBurrow", true, "Break target burrow");
    public Setting<Boolean> noweb = new Setting<>("NotWeb", true, "Don't break target web", v -> burrow.getValue());
    public Setting<Boolean> noskull = new Setting<>("NotSkull", false, "Don't break target skull", v -> burrow.getValue());
    public Setting<Boolean> self = new Setting<>("SelfUnTrap", true, "UnTrap you");


    private BlockPos prevPos;
    TimerUtil timerUtil = new TimerUtil();
    private volatile BlockPos currentPos;
    private volatile EnumFacing currentFacing;

    public AutoMine() {
        super("AutoMine", "", Category.COMBAT);
    }


    @EventListener
    public void onMotion(MotionEvent.Pre event) {
        if (currentPos != null)
            if ((BlockUtil.canSee(currentPos)
                    ? range.getValue() * range.getValue()
                    : wallRange.getValue() * wallRange.getValue()) >= mc.player.getDistanceSq(currentPos)) {
                currentPos = null;
                prevPos = null;
                currentFacing = null;
            }

        if (currentPos == null || currentFacing == null || timerUtil.passedMs(10000)) {
            calc();
        }
    }

    public void calc() {
        try {
            BlockPos bestPos = getPos();
            if (bestPos != null) {
                EnumFacing facing = interactionManager.getFacing(bestPos, strictDirection.getValue());
                if (facing != null) {
                    currentPos = bestPos;
                    currentFacing = facing;
                    timerUtil.reset();
                    if (mode.getValue().equals(Mode.Forever)) {
                        if (!SpeedMine.setBlock(bestPos, facing)) {
                            disable("Enable SpeedMine.");
                        }
                    } else {
                        mc.playerController.onPlayerDamageBlock(bestPos, facing);
                    }

                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange && currentPos != null) {
            if (((SPacketBlockChange) event.getPacket()).getBlockPosition().equals(currentPos) && BlockUtil.isPosEmpty(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                prevPos = currentPos;
            }
            calc();
        }
    }

    @Override
    public void onEnable() {
        currentPos = null;
        currentFacing = null;
        prevPos = null;
    }

    public BlockPos getPos() {
        if (self.getValue()) {
            BlockPos blockPos = getUnTrapPos();
            if (blockPos != null) return blockPos;
        }

        EntityPlayer target = getTarget();
        if (target == null) return null;
        BlockPos targetPos = new BlockPos(target.getPositionVector());

        if (burrow.getValue()) {
            float hardness = mc.world.getBlockState(targetPos).getBlockHardness(mc.world, targetPos);
            boolean check = mc.world.getBlockState(targetPos).getBlock().equals(Blocks.WEB) && noweb.getValue();
            boolean check1 = mc.world.getBlockState(targetPos).getBlock().equals(Blocks.SKULL) && noskull.getValue();
            if (hardness > 0 &&
                    !BlockUtil.isPosEmpty(targetPos) &&
                    !check &&
                    !check1) return targetPos;
        }

        BlockPos blockPos1 = VulnerabilityUtil.getVulnerablePositions(targetPos).stream()
                .filter(blockPos -> (BlockUtil.canSee(blockPos)
                        ? range.getValue() * range.getValue()
                        : wallRange.getValue() * wallRange.getValue()) >= mc.player.getDistanceSq(blockPos))
                .min(Comparator.comparing(pos -> mc.player.getDistanceSq(pos)))
                .orElse(null);
        if (blockPos1 != null) return blockPos1;

        blockPos1 = Stream.of(targetPos.east().up(), targetPos.west().up(), targetPos.north().up(), targetPos.south().up(), targetPos.up().up())
                .filter(blockPos -> !BlockUtil.isPosEmpty(blockPos))
                .filter(blockPos -> BlockUtil.isPosEmpty(blockPos.up()))
                .filter(blockPos -> (BlockUtil.canSee(blockPos)
                        ? range.getValue() * range.getValue()
                        : wallRange.getValue() * wallRange.getValue()) >= mc.player.getDistanceSq(blockPos))
                .min(Comparator.comparing(pos -> mc.player.getDistanceSq(pos)))
                .orElse(null);

        return blockPos1;
    }

    public BlockPos getUnTrapPos() {
        BlockPos self = new BlockPos(mc.player.getPositionVector()).up(2);
        float hardness = mc.world.getBlockState(self).getBlockHardness(mc.world, self);
        if (hardness > 0 && !BlockUtil.isPosEmpty(self)) return self;

        self = new BlockPos(mc.player.getPositionVector()).up(3);
        hardness = mc.world.getBlockState(self).getBlockHardness(mc.world, self);
        if (hardness > 0 && !BlockUtil.isPosEmpty(self)) return self;
        return null;
    }

    private EntityPlayer getTarget() {
        return mc.world.playerEntities.stream()
                .filter(Objects::nonNull)
                .filter(entityPlayer -> !entityPlayer.isDead)
                .filter(entityPlayer -> !EntityUtil.isntValid(entityPlayer, 11))
                .min(Comparator.comparing(entityPlayer -> entityPlayer.getDistanceSq(mc.player))).orElse(null);
    }

    public enum Mode {
        All,
        Forever
    }
}
