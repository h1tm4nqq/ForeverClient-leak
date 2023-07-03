package we.devs.forever.api.manager.impl.player.interact;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.Client;

import java.util.Arrays;
import java.util.Comparator;

public class ClickLocation extends Client {

    private final Vec3d hit;
    private final BlockPos neighbour;
    private final EnumFacing facing;

    public ClickLocation(BlockPos neighbour, EnumFacing facing, Vec3d vec3d) {
        hit = vec3d;
        this.neighbour = neighbour;
        this.facing = facing;
    }

    public Vec3d getHit() {
        return hit;
    }

    public BlockPos getNeighbour() {
        return neighbour;
    }

    public EnumFacing getFacing() {
        return facing;
    }


    public static ClickLocation getClick(BlockPos pos, boolean strict) {
        if (pos == null || fullNullCheck()) return null;
        Vec3d eyePos = mc.player.getPositionEyes(1.0f);
        EnumFacing facing1;
        if (strict) {
            facing1 = Arrays.stream(EnumFacing.values())
                    .filter(facing -> mc.world.getBlockState(pos.offset(facing)).getBlock().canCollideCheck(mc.world.getBlockState(pos.offset(facing)), false))
                    .filter(facing ->BlockUtil.getVisibleSides(pos).contains(facing.getOpposite()))
                    .filter(facing -> !mc.world.getBlockState(pos.offset(facing)).getMaterial().isReplaceable())
                    .min(Comparator.comparing(facing -> eyePos.squareDistanceTo(new Vec3d(pos.offset(facing))))).orElse(EnumFacing.UP);
        } else {
            facing1 = Arrays.stream(EnumFacing.values())
                    .filter(facing -> mc.world.getBlockState(pos.offset(facing)).getBlock().canCollideCheck(mc.world.getBlockState(pos.offset(facing)), false))
                    .filter(facing -> !mc.world.getBlockState(pos.offset(facing)).getMaterial().isReplaceable())
                    .min(Comparator.comparing(facing -> eyePos.squareDistanceTo(new Vec3d(pos.offset(facing))))).orElse(EnumFacing.UP);
        }

        BlockPos neighbour = pos.offset(facing1);

        float[] lol = rotationManager.getAngle(new Vec3d(neighbour));
        Vec3d placeVector = rotationManager.getVectorForRotation(lol[0], lol[1]);

        RayTraceResult interactVector = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), mc.player.getPositionEyes(1).add(placeVector.x, placeVector.y, placeVector.z), false, false, true);

        Vec3d vec3d;
        if (interactVector == null) vec3d = new Vec3d(neighbour).add(0.5, 0.5, 0.5);
        else vec3d = placeVector;

        return new ClickLocation(neighbour, facing1.getOpposite(), vec3d);
    }


}
