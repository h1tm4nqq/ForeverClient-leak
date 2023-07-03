package we.devs.forever.api.util.math.path;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.Client;

import java.util.ArrayList;
import java.util.List;

public class BasePathGroup extends Client {
    private List<BasePath> paths = new ArrayList<>();
    private final BlockPos endPos;
    private final int maxHelping;
    private final float wallRange;
    private final float range;
    private final EnumFacing direction;
    private int count = 1;

    public BasePathGroup(BlockPos endPos, EnumFacing direction, int maxHelping, float wallRange, float range) {
        this.endPos = endPos;
        this.maxHelping = maxHelping;
        this.direction = direction;
        this.wallRange = wallRange;
        this.range = range;
    }

    @Override
    public String toString() {
        return "BasePathGroup{" +
                "paths=" + paths +
                '}';
    }

    public boolean findPath() {
        List<BasePath> temp = findByDirection(new BasePath(endPos, 0, this));
        if (temp == null) return false;
        paths = temp;
        return true;
    }

//    @SuppressWarnings("all")
//    private List<BasePath> getGroup() {
//        List<BasePathGroup> temp = new ArrayList<>();
//        BlockPos old = endPos;
//        while (count <= maxHelping + 1) {
//            old = old.offset(direction);
//            BlockPos finalOld = old;
//            Arrays.stream(EnumFacing.values()).filter(enumFacing -> {
//                if (direction == EnumFacing.EAST || direction == EnumFacing.WEST) {
//                    return enumFacing != EnumFacing.EAST && enumFacing != EnumFacing.WEST;
//                }
//                if (direction == EnumFacing.NORTH || direction == EnumFacing.SOUTH) {
//                    return enumFacing != EnumFacing.SOUTH && enumFacing != EnumFacing.NORTH;
//                }
//                if (direction == EnumFacing.UP || direction == EnumFacing.DOWN) {
//                    return enumFacing != EnumFacing.UP && enumFacing != EnumFacing.DOWN;
//                }
//                return true;
//            }).forEach(enumFacing -> temp.add(new BasePathGroup(finalOld.offset(enumFacing), enumFacing, maxHelping, true)));
//            count++;
//        }
//        return temp.parallelStream()
//                .filter(basePathGroup -> basePathGroup.findPath())
//                .min(Comparator.comparingDouble(value -> value.getCount())).orElse(null).paths;
//
//    }

    public boolean checkInRage(BasePath path) {
            return BlockUtil.canSee(path.getPos())
                    ? mc.player.getPositionEyes(1F).squareDistanceTo(new Vec3d(path.getPos().add(0.5, 0, 0.5))) < range *range
                    : mc.player.getPositionEyes(1F).distanceTo(new Vec3d(path.getPos().add(0.5, 0, 0.5))) < wallRange *wallRange;
    }

    private List<BasePath> findByDirection(BasePath path) {
        List<BasePath> temp = new ArrayList<>();
        BasePath old = path;
        boolean find = false;
        temp.add(path);
        while (count <= maxHelping) {
            old = new BasePath(old.getPos().offset(direction), count, this);
            if(!checkInRage(old)) break;
            temp.add(old);
            count++;
            if (canPlace(old.getPos())) {
                find = true;
                break;
            }
        }
        return find ? temp : null;
    }

    public List<BasePath> getPaths() {
        return paths;
    }

    public int getCount() {
        return count;
    }

    private static boolean canPlace(BlockPos pos) {
        for (EnumFacing side : BlockUtil.getPossibleSides(pos,false)) {
            if (BlockUtil.canBeClicked(pos.offset(side))) {
                return true;
            }
        }
        return false;
    }
}
