package we.devs.forever.api.util.math.path;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.Client;

import java.util.*;
import java.util.stream.Collectors;

public class PathUtil extends Client {


    public static List<BasePath> findPath(BlockPos end, float wallRange, float range,int helpingBlocks) {
        List<BasePath> temp = new ArrayList<>();
        if (end == null) return temp;
        if (canPlace(end)) {
            temp.add(new BasePath(end,1,null));
            return temp;
        }
        if(helpingBlocks <= 0) return temp;
        BasePathGroup group = Arrays.stream(EnumFacing.values())
                .map(enumFacing -> new BasePathGroup(end, enumFacing, helpingBlocks, wallRange, range))
                .filter(BasePathGroup::findPath)
                .min(Comparator.comparingDouble(BasePathGroup::getCount)).orElse(null);
        if (group == null) return temp;
        temp.addAll(group.getPaths().stream()
                .filter(path -> path != null)

                .collect(Collectors.toList())
        );
        temp = temp.stream().sorted(Comparator.comparing(path -> path.getPos().distanceSq(end.getX(),end.getY(),end.getZ()))).collect(Collectors.toList());
        Collections.reverse(temp);

        return temp;
    }

    private static double increment(double value) {
        if (value == 0) return 0;
        if (value < 0) return value + 1;
        else return value - 1;
    }

    //BlockUtil.isP++osEmpty(pos)

    private static boolean checkRange(BlockPos pos, float wallRange, float range) {
        return BlockUtil.getEyesPos().squareDistanceTo(pos.getX() + 0.5F, pos.getY(), pos.getX() + 0.5F) <= (BlockUtil.canSee(pos) ? wallRange * wallRange : range * range);
    }

    private static boolean canPlace(BlockPos pos) {
        for (EnumFacing side : BlockUtil.getPossibleSides(pos,false)) {
            if (BlockUtil.canBeClicked(pos.offset(side))) {
                return true;
            }
        }
        return false;
    }

    private static boolean doChecksCrystal(BlockPos pos) {
        for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity instanceof EntityExpBottle)
                continue;
            return false;
        }
        return true;
    }
}
