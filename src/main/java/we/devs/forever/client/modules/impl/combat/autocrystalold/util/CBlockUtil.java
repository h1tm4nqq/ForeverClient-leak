package we.devs.forever.client.modules.impl.combat.autocrystalold.util;

import net.minecraft.util.math.BlockPos;

public class CBlockUtil {

    public static boolean sameBlockPos(BlockPos first, BlockPos second) {
        if (first == null || second == null)
            return false;
        return first.getX() == second.getX() && first.getY() == second.getY() && first.getZ() == second.getZ();
    }
}
