package we.devs.forever.client.modules.impl.combat.autocrystalold.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;



public interface AutoCrystalQueue {

    void addPlacePos(BlockPos pos);
    void addBreakEntity(Entity pos);

}
